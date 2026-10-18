package jp.co.santa.soft.tui.support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import jp.co.santa.soft.tui.config.TamboUiConfig;
import jp.co.santa.soft.tui.core.model.SystemTask;
import jp.co.santa.soft.tui.core.service.SystemTaskService;

/**
 * Presenter/Controller mediating between TamboUI views and Quarkus backend services. Using
 * package-private fields avoids reflection in GraalVM native images.
 */
@ApplicationScoped
public class TuiPresenter {

  @Inject SystemTaskService taskService;

  @Inject TamboUiConfig config;

  private int selectedIndex = 0;

  public List<SystemTask> getTasks() {
    return taskService.findAllTasks();
  }

  public SystemTask getSelectedTask() {
    return taskService.findByIndex(selectedIndex);
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void selectNext() {
    int size = taskService.findAllTasks().size();
    if (size > 0) {
      selectedIndex = (selectedIndex + 1) % size;
    }
  }

  public void selectPrevious() {
    int size = taskService.findAllTasks().size();
    if (size > 0) {
      selectedIndex = (selectedIndex - 1 + size) % size;
    }
  }

  public void triggerAdvanceOnSelected() {
    taskService.advanceProgress(selectedIndex);
  }

  public TamboUiConfig getConfig() {
    return config;
  }
}
