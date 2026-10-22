package jp.co.santa.soft.tui.reducer;

import java.util.ArrayList;
import java.util.List;
import jp.co.santa.soft.tui.cmd.Cmd;
import jp.co.santa.soft.tui.core.model.ActivePane;
import jp.co.santa.soft.tui.core.model.AppState;
import jp.co.santa.soft.tui.core.model.DaemonEntry;
import jp.co.santa.soft.tui.core.model.PipelineStatus;
import jp.co.santa.soft.tui.core.model.PipelineStep;
import jp.co.santa.soft.tui.msg.AppMsg;

public final class AppReducer {

  private AppReducer() {}

  public static UpdateResult update(AppState state, AppMsg msg) {
    if (state.shouldExit()) {
      return UpdateResult.only(state);
    }

    return switch (msg) {
      case AppMsg.NextPane _ ->
          UpdateResult.only(state.withActivePane(nextPane(state.activePane())));
      case AppMsg.PrevPane _ ->
          UpdateResult.only(state.withActivePane(prevPane(state.activePane())));
      case AppMsg.SelectPane(ActivePane pane) -> UpdateResult.only(state.withActivePane(pane));

      case AppMsg.NextItem _ -> UpdateResult.only(navigateItem(state, 1));
      case AppMsg.PrevItem _ -> UpdateResult.only(navigateItem(state, -1));
      case AppMsg.SelectUnit(int index) -> UpdateResult.only(state.withSelectedUnitIndex(index));

      case AppMsg.TriggerCurrentAction _ -> handleTriggerCurrentAction(state);

      case AppMsg.RequestQuit _ -> UpdateResult.only(state.withQuitModal(true));
      case AppMsg.CancelQuit _ -> UpdateResult.only(state.withQuitModal(false));
      case AppMsg.ConfirmQuit(boolean unregisterKey) -> {
        AppState closingState = state.withQuitModal(false).withShouldExit(true);
        if (unregisterKey) {
          yield UpdateResult.of(closingState, new Cmd.UnregisterSshKey(state.currentUnit()));
        }
        yield UpdateResult.only(closingState);
      }

      case AppMsg.StepStarted(int stepId) -> {
        List<PipelineStep> updated =
            updateStepStatus(state.pipelineSteps(), stepId, PipelineStatus.RUNNING, "Executing...");
        AppState newState =
            state.withPipelineSteps(updated).withAppendedLog("▶ Step " + stepId + " started.");
        yield UpdateResult.only(newState);
      }

      case AppMsg.StepProgress(int stepId, String logLine) -> {
        List<PipelineStep> updated =
            updateStepStatus(state.pipelineSteps(), stepId, PipelineStatus.RUNNING, logLine);
        AppState newState =
            state.withPipelineSteps(updated).withAppendedLog("[" + stepId + "] " + logLine);
        yield UpdateResult.only(newState);
      }

      case AppMsg.StepCompleted(int stepId, PipelineStatus status, String summary) -> {
        List<PipelineStep> updated =
            updateStepStatus(state.pipelineSteps(), stepId, status, summary);
        String icon = status == PipelineStatus.SUCCESS ? "✔" : "✖";
        AppState newState =
            state
                .withPipelineSteps(updated)
                .withAppendedLog(icon + " Step " + stepId + " finished: " + summary);
        yield UpdateResult.only(newState);
      }

      case AppMsg.DaemonStatusChanged(String name, int pid, boolean running) -> {
        List<DaemonEntry> updated =
            state.daemons().stream()
                .map(d -> d.name().equals(name) ? d.withRunning(running, pid) : d)
                .toList();
        String statusStr = running ? "RUNNING (PID " + pid + ")" : "STOPPED";
        AppState newState =
            state.withDaemons(updated).withAppendedLog("Daemon " + name + " is now " + statusStr);
        yield UpdateResult.only(newState);
      }

      case AppMsg.LogAppended(String logLine) -> UpdateResult.only(state.withAppendedLog(logLine));
    };
  }

  private static UpdateResult handleTriggerCurrentAction(AppState state) {
    if (state.quitConfirmationModal()) {
      return UpdateResult.only(state);
    }

    return switch (state.activePane()) {
      case PIPELINE -> {
        if (state.pipelineSteps().isEmpty()) {
          yield UpdateResult.only(state);
        }
        PipelineStep step = state.pipelineSteps().get(state.selectedStepIndex());
        List<PipelineStep> updated =
            updateStepStatus(
                state.pipelineSteps(), step.id(), PipelineStatus.RUNNING, "Triggered by user...");
        AppState newState =
            state
                .withPipelineSteps(updated)
                .withAppendedLog("Triggering pipeline step " + step.id() + ": " + step.title());
        yield UpdateResult.of(newState, new Cmd.RunPipelineStep(step.id(), state.currentUnit()));
      }
      case SERVICES -> {
        if (state.daemons().isEmpty()) {
          yield UpdateResult.only(state);
        }
        DaemonEntry daemon = state.daemons().get(state.selectedServiceIndex());
        AppState newState = state.withAppendedLog("Toggling daemon: " + daemon.name());
        String action = daemon.running() ? "stop" : "start";
        yield UpdateResult.of(
            newState, new Cmd.ControlDaemon(daemon.name(), action, state.currentUnit()));
      }
      default -> UpdateResult.only(state);
    };
  }

  private static ActivePane nextPane(ActivePane current) {
    return switch (current) {
      case UNITS -> ActivePane.PIPELINE;
      case PIPELINE -> ActivePane.FILES;
      case FILES -> ActivePane.SERVICES;
      case SERVICES -> ActivePane.INSPECTOR;
      case INSPECTOR -> ActivePane.UNITS;
    };
  }

  private static ActivePane prevPane(ActivePane current) {
    return switch (current) {
      case UNITS -> ActivePane.INSPECTOR;
      case PIPELINE -> ActivePane.UNITS;
      case FILES -> ActivePane.PIPELINE;
      case SERVICES -> ActivePane.FILES;
      case INSPECTOR -> ActivePane.SERVICES;
    };
  }

  private static AppState navigateItem(AppState state, int delta) {
    return switch (state.activePane()) {
      case UNITS -> state.withSelectedUnitIndex(state.selectedUnitIndex() + delta);
      case PIPELINE -> state.withSelectedStepIndex(state.selectedStepIndex() + delta);
      case FILES -> state.withSelectedFileIndex(state.selectedFileIndex() + delta);
      case SERVICES -> state.withSelectedServiceIndex(state.selectedServiceIndex() + delta);
      case INSPECTOR -> state;
    };
  }

  private static List<PipelineStep> updateStepStatus(
      List<PipelineStep> steps, int stepId, PipelineStatus newStatus, String log) {
    List<PipelineStep> list = new ArrayList<>(steps.size());
    for (PipelineStep s : steps) {
      if (s.id() == stepId) {
        list.add(s.withStatus(newStatus, log));
      } else {
        list.add(s);
      }
    }
    return List.copyOf(list);
  }
}
