package jp.co.santa.soft.tui.core.model;

public record DaemonEntry(String name, String scriptPath, int pid, boolean running) {
  public DaemonEntry withRunning(boolean isRunning, int newPid) {
    return new DaemonEntry(name, scriptPath, newPid, isRunning);
  }
}
