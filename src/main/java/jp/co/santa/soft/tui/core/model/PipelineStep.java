package jp.co.santa.soft.tui.core.model;

public record PipelineStep(
    int id, String title, String description, PipelineStatus status, String detailLog) {
  public PipelineStep withStatus(PipelineStatus newStatus, String newLog) {
    return new PipelineStep(id, title, description, newStatus, newLog);
  }
}
