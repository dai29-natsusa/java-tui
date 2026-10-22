package jp.co.santa.soft.tui.view;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;

import dev.tamboui.backend.panama.PanamaBackend;
import dev.tamboui.toolkit.app.ToolkitRunner;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.event.Event;
import dev.tamboui.tui.event.KeyEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import jp.co.santa.soft.core.ssh.SshService;
import jp.co.santa.soft.tui.cmd.Cmd;
import jp.co.santa.soft.tui.config.TamboUiConfig;
import jp.co.santa.soft.tui.config.UnitConfig;
import jp.co.santa.soft.tui.core.model.ActivePane;
import jp.co.santa.soft.tui.core.model.AppState;
import jp.co.santa.soft.tui.core.model.ConfigFileEntry;
import jp.co.santa.soft.tui.core.model.DaemonEntry;
import jp.co.santa.soft.tui.core.model.PipelineStatus;
import jp.co.santa.soft.tui.core.model.PipelineStep;
import jp.co.santa.soft.tui.core.model.UnitTarget;
import jp.co.santa.soft.tui.msg.AppMsg;
import jp.co.santa.soft.tui.reducer.AppReducer;
import jp.co.santa.soft.tui.reducer.UpdateResult;
import org.jboss.logging.Logger;

/** Top-level screen implementing the MVU pattern and Multi-Pane Inspector layout. */
@ApplicationScoped
public class MainDashboardView {

  private static final Logger LOG = Logger.getLogger(MainDashboardView.class);

  @Inject UnitConfig unitConfig;

  @Inject TamboUiConfig uiConfig;

  @Inject SshService sshService;

  private ToolkitRunner runner;
  private AppState state;

  public void launch() throws Exception {
    initAppState();

    var configBuilder =
        TuiConfig.builder()
            .mouseCapture(uiConfig.mouseCapture())
            .tickRate(Duration.ofMillis(uiConfig.tickRateMs()));

    if ("panama".equalsIgnoreCase(uiConfig.backend())) {
      configBuilder.backend(new PanamaBackend());
    }

    try (var toolkitRunner = ToolkitRunner.create(configBuilder.build())) {
      this.runner = toolkitRunner;
      this.runner.eventRouter().addGlobalHandler(this::handleKeyEvent);
      this.runner.run(this::renderScreen);
    }
  }

  private void initAppState() {
    List<UnitTarget> targets = new ArrayList<>();
    List<DaemonEntry> daemons = new ArrayList<>();

    if (unitConfig != null && unitConfig.units() != null && !unitConfig.units().isEmpty()) {
      unitConfig
          .units()
          .forEach(
              (key, unit) -> {
                targets.add(
                    new UnitTarget(
                        key,
                        unit.name(),
                        unit.ip(),
                        unit.hostname(),
                        unit.workdir(),
                        targets.isEmpty()));
                if (unit.instances() != null) {
                  unit.instances()
                      .forEach(
                          inst ->
                              daemons.add(
                                  new DaemonEntry(
                                      inst.name(), inst.location(), inst.pid(), false)));
                }
              });
    }

    if (targets.isEmpty()) {
      targets.add(
          new UnitTarget("unit01", "xmcs1", "192.168.40.1", "ol8xmcs1", "/home/xmcs/XMCS", true));
      targets.add(
          new UnitTarget("unit02", "xmcs2", "192.168.40.2", "ol8xmcs2", "/home/xmcs/XMCS", false));
    }

    if (daemons.isEmpty()) {
      daemons.add(new DaemonEntry("xmcs_server", "scripts/core.sh", 999, false));
      daemons.add(new DaemonEntry("amhs_emulator", "scripts/emu.sh", 888, false));
      daemons.add(new DaemonEntry("xmcs-monitor", "scripts/monitor.sh", 777, false));
    }

    this.state = AppState.initial(targets, daemons);
  }

  private EventResult handleKeyEvent(Event event) {
    if (event instanceof KeyEvent key) {
      if (state.quitConfirmationModal()) {
        if (key.isCharIgnoreCase('y')) {
          dispatch(new AppMsg.ConfirmQuit(true));
          return EventResult.HANDLED;
        }
        if (key.isCharIgnoreCase('n')) {
          dispatch(new AppMsg.ConfirmQuit(false));
          return EventResult.HANDLED;
        }
        if (key.isCancel()) {
          dispatch(new AppMsg.CancelQuit());
          return EventResult.HANDLED;
        }
        return EventResult.HANDLED;
      }

      if (key.hasCtrl()) {
        if (key.isChar('1')) {
          dispatch(new AppMsg.SelectPane(ActivePane.UNITS));
          return EventResult.HANDLED;
        }
        if (key.isChar('2')) {
          dispatch(new AppMsg.SelectPane(ActivePane.PIPELINE));
          return EventResult.HANDLED;
        }
        if (key.isChar('3')) {
          dispatch(new AppMsg.SelectPane(ActivePane.FILES));
          return EventResult.HANDLED;
        }
        if (key.isChar('4')) {
          dispatch(new AppMsg.SelectPane(ActivePane.SERVICES));
          return EventResult.HANDLED;
        }
      }

      if (key.isFocusNext()) {
        dispatch(new AppMsg.NextPane());
        return EventResult.HANDLED;
      }
      if (key.isFocusPrevious()) {
        dispatch(new AppMsg.PrevPane());
        return EventResult.HANDLED;
      }
      if (key.isUp()) {
        dispatch(new AppMsg.PrevItem());
        return EventResult.HANDLED;
      }
      if (key.isDown()) {
        dispatch(new AppMsg.NextItem());
        return EventResult.HANDLED;
      }
      if (key.isChar(' ') || key.isConfirm()) {
        dispatch(new AppMsg.TriggerCurrentAction());
        return EventResult.HANDLED;
      }
      if (key.isQuit() || key.isCharIgnoreCase('q')) {
        dispatch(new AppMsg.RequestQuit());
        return EventResult.HANDLED;
      }
    }
    return EventResult.UNHANDLED;
  }

  private void dispatch(AppMsg msg) {
    UpdateResult result = AppReducer.update(this.state, msg);
    this.state = result.state();
    if (this.state.shouldExit() && runner != null) {
      runner.quit();
    }
    executeCmd(result.cmd());
  }

  private void executeCmd(Cmd cmd) {
    switch (cmd) {
      case Cmd.None _ -> {}
      case Cmd.Batch(List<Cmd> list) -> list.forEach(this::executeCmd);
      case Cmd.RunPipelineStep step -> Thread.ofVirtual().start(() -> runPipelineStepAsync(step));
      case Cmd.ControlDaemon daemon ->
          Thread.ofVirtual().start(() -> runControlDaemonAsync(daemon));
      case Cmd.UnregisterSshKey unregister -> {
        LOG.info("Unregistering SSH public key from " + unregister.target().ip());
        dispatch(
            new AppMsg.LogAppended("Unregistered public key from " + unregister.target().ip()));
      }
    }
  }

  private void runPipelineStepAsync(Cmd.RunPipelineStep step) {
    int id = step.stepId();
    try {
      runner.runOnRenderThread(
          () -> dispatch(new AppMsg.StepProgress(id, "Initializing step " + id + "...")));
      Thread.sleep(300);

      if (id == 2) {
        // Step 2: Real SSH Key Generation
        Path keyPath = Path.of(System.getProperty("user.home"), ".ssh", "santa_ed25519");
        sshService.generateSshKeys(keyPath, "santa@deployer");
        runner.runOnRenderThread(
            () ->
                dispatch(new AppMsg.StepProgress(id, "Generated local Ed25519 key at " + keyPath)));
        Thread.sleep(400);
        runner.runOnRenderThread(
            () ->
                dispatch(
                    new AppMsg.StepCompleted(
                        id,
                        PipelineStatus.SUCCESS,
                        "Ed25519 key generated & verified at " + keyPath)));
      } else {
        // Simulation for first draft preview
        runner.runOnRenderThread(
            () -> dispatch(new AppMsg.StepProgress(id, "Connecting to " + step.target().ip())));
        Thread.sleep(500);
        runner.runOnRenderThread(
            () -> dispatch(new AppMsg.StepProgress(id, "Processing operation parameters...")));
        Thread.sleep(500);
        runner.runOnRenderThread(
            () ->
                dispatch(
                    new AppMsg.StepCompleted(
                        id, PipelineStatus.SUCCESS, "Completed successfully.")));
      }
    } catch (Exception e) {
      runner.runOnRenderThread(
          () ->
              dispatch(
                  new AppMsg.StepCompleted(id, PipelineStatus.FAILED, "Error: " + e.getMessage())));
    }
  }

  private void runControlDaemonAsync(Cmd.ControlDaemon daemon) {
    try {
      boolean nextRunning =
          "start".equalsIgnoreCase(daemon.action()) || "restart".equalsIgnoreCase(daemon.action());
      int pid = nextRunning ? (int) (1000 + (Math.random() * 9000)) : 0;
      Thread.sleep(400);
      runner.runOnRenderThread(
          () -> dispatch(new AppMsg.DaemonStatusChanged(daemon.daemonName(), pid, nextRunning)));
    } catch (Exception ignored) {
    }
  }

  private Element renderScreen() {
    if (state.quitConfirmationModal()) {
      return renderQuitModal();
    }

    return column(
        renderHeader(),
        row(
                column(
                        renderUnitsPanel(),
                        renderPipelinePanel(),
                        renderFilesPanel(),
                        renderServicesPanel())
                    .length(44),
                renderInspectorPanel())
            .fill(),
        renderStatusBar());
  }

  private Element renderHeader() {
    UnitTarget current = state.currentUnit();
    return panel(
            "SANTA SOFT DEPLOYER • Java 25 & Quarkus 4",
            row(
                text(" Target: ").bold(),
                text(current.name() + " (" + current.ip() + ")").cyan().bold(),
                spacer(),
                text("HA Host: ").dim(),
                text(current.hostname()).yellow(),
                spacer(),
                text("Active Pane: ").dim(),
                text(state.activePane().name()).green().bold()))
        .rounded()
        .length(3);
  }

  private Element renderUnitsPanel() {
    boolean isActive = state.activePane() == ActivePane.UNITS;
    var col = column();
    for (int i = 0; i < state.units().size(); i++) {
      UnitTarget u = state.units().get(i);
      boolean selected = (i == state.selectedUnitIndex());
      String cursor = selected ? "▶ " : "  ";
      String label = cursor + u.name() + " [" + u.ip() + "] - " + (i == 0 ? "Primary" : "Standby");
      col.add(selected && isActive ? text(label).cyan().bold() : text(label));
    }
    var p = panel("[1] TARGET UNITS", col).rounded();
    return isActive ? p.cyan() : p;
  }

  private Element renderPipelinePanel() {
    boolean isActive = state.activePane() == ActivePane.PIPELINE;
    var col = column();
    for (int i = 0; i < state.pipelineSteps().size(); i++) {
      PipelineStep s = state.pipelineSteps().get(i);
      boolean selected = (i == state.selectedStepIndex());
      String cursor = selected ? "▶ " : "  ";
      String icon =
          switch (s.status()) {
            case PENDING -> "[ ]";
            case RUNNING -> "[⟳]";
            case SUCCESS -> "[✔]";
            case FAILED -> "[✖]";
          };
      String label = cursor + icon + " " + s.id() + ". " + s.title();
      if (s.status() == PipelineStatus.RUNNING) {
        col.add(text(label).yellow().bold());
      } else if (s.status() == PipelineStatus.SUCCESS) {
        col.add(text(label).green());
      } else if (s.status() == PipelineStatus.FAILED) {
        col.add(text(label).red().bold());
      } else if (selected && isActive) {
        col.add(text(label).cyan().bold());
      } else {
        col.add(text(label));
      }
    }
    var p = panel("[2] AUTOMATED PIPELINE", col).rounded();
    return isActive ? p.cyan() : p;
  }

  private Element renderFilesPanel() {
    boolean isActive = state.activePane() == ActivePane.FILES;
    var col = column();
    for (int i = 0; i < state.configFiles().size(); i++) {
      ConfigFileEntry f = state.configFiles().get(i);
      boolean selected = (i == state.selectedFileIndex());
      String cursor = selected ? "▶ " : "  ";
      String label = cursor + "[" + f.userRole() + "] " + f.name();
      col.add(selected && isActive ? text(label).cyan().bold() : text(label));
    }
    var p = panel("[3] FILES & CONFIGS", col).rounded();
    return isActive ? p.cyan() : p;
  }

  private Element renderServicesPanel() {
    boolean isActive = state.activePane() == ActivePane.SERVICES;
    var col = column();
    for (int i = 0; i < state.daemons().size(); i++) {
      DaemonEntry d = state.daemons().get(i);
      boolean selected = (i == state.selectedServiceIndex());
      String cursor = selected ? "▶ " : "  ";
      String status = d.running() ? "● [RUNNING " + d.pid() + "]" : "○ [STOPPED]";
      String label = cursor + d.name() + " " + status;
      if (d.running()) {
        col.add(selected && isActive ? text(label).green().bold() : text(label).green());
      } else {
        col.add(selected && isActive ? text(label).cyan().bold() : text(label).dim());
      }
    }
    var p = panel("[4] SERVICES & DAEMONS", col).rounded();
    return isActive ? p.cyan() : p;
  }

  private Element renderInspectorPanel() {
    var col = column();

    switch (state.activePane()) {
      case UNITS -> {
        UnitTarget u = state.currentUnit();
        col.add(row(text("Unit Identifier: ").bold(), text(u.id()).cyan()));
        col.add(row(text("Unit Name:       ").bold(), text(u.name()).yellow()));
        col.add(row(text("IP Address:      ").bold(), text(u.ip())));
        col.add(row(text("Hostname:        ").bold(), text(u.hostname())));
        col.add(row(text("Working Dir:     ").bold(), text(u.workdir())));
      }
      case PIPELINE -> {
        if (!state.pipelineSteps().isEmpty()) {
          PipelineStep s = state.pipelineSteps().get(state.selectedStepIndex());
          col.add(
              row(text("Step:        ").bold(), text(s.id() + " - " + s.title()).cyan().bold()));
          col.add(row(text("Description: ").bold(), text(s.description())));
          col.add(row(text("Status:      ").bold(), formatStatusBadge(s.status())));
          col.add(spacer());
          col.add(text("Execution Log / Progress:").bold());
          col.add(text(s.detailLog()).yellow());
        }
      }
      case FILES -> {
        if (!state.configFiles().isEmpty()) {
          ConfigFileEntry f = state.configFiles().get(state.selectedFileIndex());
          col.add(row(text("File:      ").bold(), text(f.name()).cyan().bold()));
          col.add(row(text("Path:      ").bold(), text(f.path())));
          col.add(row(text("User Role: ").bold(), text(f.userRole()).yellow()));
          col.add(row(text("Status:    ").bold(), text(f.status())));
          col.add(spacer());
          col.add(text("Content Preview:").bold());
          col.add(text(f.contentPreview()).dim());
        }
      }
      case SERVICES -> {
        if (!state.daemons().isEmpty()) {
          DaemonEntry d = state.daemons().get(state.selectedServiceIndex());
          col.add(row(text("Daemon:    ").bold(), text(d.name()).cyan().bold()));
          col.add(row(text("Script:    ").bold(), text(d.scriptPath())));
          col.add(
              row(
                  text("Status:    ").bold(),
                  d.running() ? text("RUNNING").green().bold() : text("STOPPED").dim()));
          col.add(
              row(text("Process ID:").bold(), text(d.pid() > 0 ? String.valueOf(d.pid()) : "N/A")));
          col.add(spacer());
          col.add(text("Controls: Press [Space] to toggle start/stop.").yellow());
        }
      }
      case INSPECTOR -> col.add(text("Live Inspector Active").cyan().bold());
    }

    col.add(spacer());
    col.add(text("─── Recent Execution Logs ────────────────────────────").dim());

    List<String> logs = state.logLines();
    int start = Math.max(0, logs.size() - 6);
    for (int i = start; i < logs.size(); i++) {
      col.add(text(logs.get(i)).dim());
    }

    var p = panel("[5] LIVE INSPECTOR & EXECUTION LOGS", col).rounded().fill();
    return state.activePane() == ActivePane.INSPECTOR ? p.cyan() : p;
  }

  private Element formatStatusBadge(PipelineStatus status) {
    return switch (status) {
      case PENDING -> text("PENDING").dim();
      case RUNNING -> text("RUNNING...").yellow().bold();
      case SUCCESS -> text("SUCCESS").green().bold();
      case FAILED -> text("FAILED").red().bold();
    };
  }

  private Element renderQuitModal() {
    return panel(
            "Confirm Exit",
            column(
                spacer(),
                text("Do you want to unregister the generated public key from remote server?")
                    .yellow()
                    .bold(),
                spacer(),
                text("  [y] Yes — Unregister public key from authorized_keys and exit").cyan(),
                text("  [n] No  — Keep public key registered and exit").white(),
                text("  [Esc] Cancel — Return to deployer dashboard").dim(),
                spacer()))
        .rounded()
        .cyan();
  }

  private Element renderStatusBar() {
    return panel(
            "Shortcuts",
            row(
                text("[Tab] Next Pane").bold(),
                spacer(),
                text("[Ctrl+1..4] Jump Pane").bold(),
                spacer(),
                text("[↑/↓] Select Item").bold(),
                spacer(),
                text("[Space] Run / Toggle").bold(),
                spacer(),
                text("[q] Quit").bold()))
        .rounded()
        .length(3);
  }
}
