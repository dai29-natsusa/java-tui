package jp.co.santa.soft.tui.core.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jp.co.santa.soft.tui.core.model.SystemTask;
import jp.co.santa.soft.tui.core.model.SystemTask.TaskState;

/**
 * Pure business logic service managed by Quarkus ArC. Contains zero references to TamboUI classes.
 */
@ApplicationScoped
public class SystemTaskService {

  private final List<SystemTask> tasks =
      new CopyOnWriteArrayList<>(
          List.of(
              new SystemTask(
                  "TSK-101",
                  "Quarkus ArC Container Init",
                  "Core",
                  1.0,
                  new TaskState.Completed(Instant.now().minusSeconds(120), 42L)),
              new SystemTask(
                  "TSK-102",
                  "Panama FFM Terminal Binding",
                  "Backend",
                  0.75,
                  new TaskState.Running(4, "nagoya-node-01")),
              new SystemTask(
                  "TSK-103",
                  "GraalVM Reachability Sync",
                  "Build",
                  0.40,
                  new TaskState.Running(8, "nagoya-node-02")),
              new SystemTask(
                  "TSK-104",
                  "Legacy ANSI Stream Flush",
                  "IO",
                  0.15,
                  new TaskState.Failed("ERR_TERM_09", "Pipe closed unexpectedly"))));

  public List<SystemTask> findAllTasks() {
    return List.copyOf(tasks);
  }

  public SystemTask findByIndex(int index) {
    if (tasks.isEmpty()) {
      return null;
    }
    int safeIndex = Math.clamp(index, 0, tasks.size() - 1);
    return tasks.get(safeIndex);
  }

  public void advanceProgress(int index) {
    if (index < 0 || index >= tasks.size()) {
      return;
    }
    SystemTask current = tasks.get(index);
    double nextRatio = Math.min(1.0, current.progressRatio() + 0.10);
    TaskState nextState =
        nextRatio >= 1.0 ? new TaskState.Completed(Instant.now(), 185L) : current.state();

    tasks.set(
        index,
        new SystemTask(current.id(), current.title(), current.category(), nextRatio, nextState));
  }
}
