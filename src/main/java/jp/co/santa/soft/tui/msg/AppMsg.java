package jp.co.santa.soft.tui.msg;

import jp.co.santa.soft.tui.core.model.ActivePane;
import jp.co.santa.soft.tui.core.model.PipelineStatus;

public sealed interface AppMsg {
  // Navigation & Pane Selection
  record NextPane() implements AppMsg {}

  record PrevPane() implements AppMsg {}

  record SelectPane(ActivePane pane) implements AppMsg {}

  record NextItem() implements AppMsg {}

  record PrevItem() implements AppMsg {}

  record SelectUnit(int index) implements AppMsg {}

  // User Actions
  record TriggerCurrentAction() implements AppMsg {}

  record RequestQuit() implements AppMsg {}

  record ConfirmQuit(boolean unregisterSshKey) implements AppMsg {}

  record CancelQuit() implements AppMsg {}

  // Background System Events (from Cmd execution)
  record StepStarted(int stepId) implements AppMsg {}

  record StepProgress(int stepId, String logLine) implements AppMsg {}

  record StepCompleted(int stepId, PipelineStatus status, String summary) implements AppMsg {}

  record DaemonStatusChanged(String name, int pid, boolean running) implements AppMsg {}

  record LogAppended(String logLine) implements AppMsg {}
}
