package jp.co.santa.soft.tui.core.model;

import java.time.Instant;

/** Domain model demonstrating Java 25 records and sealed interfaces. */
public record SystemTask(
    String id, String title, String category, double progressRatio, TaskState state) {
  public sealed interface TaskState
      permits TaskState.Running, TaskState.Completed, TaskState.Failed {
    record Running(int activeWorkers, String nodeName) implements TaskState {}

    record Completed(Instant finishedAt, long durationMs) implements TaskState {}

    record Failed(String errorCode, String reason) implements TaskState {}
  }

  /** Pattern matching switch expression (Java 25) to format status details cleanly. */
  public String formattedStatus() {
    return switch (state) {
      case TaskState.Running(var workers, var node) ->
          "RUNNING (%d workers on %s)".formatted(workers, node);
      case TaskState.Completed(var finishedAt, var duration) ->
          "COMPLETED in %dms (at %s)".formatted(duration, finishedAt);
      case TaskState.Failed(var code, var reason) -> "FAILED [%s]: %s".formatted(code, reason);
    };
  }
}
