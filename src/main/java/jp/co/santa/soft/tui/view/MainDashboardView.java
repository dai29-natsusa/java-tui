package jp.co.santa.soft.tui.view;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.gauge;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.table;
import static dev.tamboui.toolkit.Toolkit.text;

import dev.tamboui.style.Color;
import dev.tamboui.toolkit.app.ToolkitRunner;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.tui.TuiConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import jp.co.santa.soft.tui.component.StatusBarComponent;
import jp.co.santa.soft.tui.core.model.SystemTask;
import jp.co.santa.soft.tui.support.TuiPresenter;

/** Top-level screen using TamboUI's Toolkit DSL and ToolkitRunner. */
@ApplicationScoped
public class MainDashboardView {

  @Inject TuiPresenter presenter;

  // Stateful ListElement managed internally by TamboUI Toolkit DSL
  private final ListElement<?> taskListElement = list().highlightColor(Color.CYAN).autoScroll();

  public void launch() throws Exception {
    var configBuilder =
        TuiConfig.builder()
            .mouseCapture(presenter.getConfig().mouseCapture())
            .tickRate(Duration.ofMillis(presenter.getConfig().tickRateMs()));

    if ("panama".equalsIgnoreCase(presenter.getConfig().backend())) {
      configBuilder.backend(new dev.tamboui.backend.panama.PanamaBackend());
    }

    var tuiConfig = configBuilder.build();

    // ToolkitRunner manages the alternate screen, Panama backend initialization, and render thread
    try (var runner = ToolkitRunner.create(tuiConfig)) {
      runner.run(this::renderScreen);
    }
  }

  /** Pure declarative rendering function invoked on TamboUI's dedicated render thread. */
  private Element renderScreen() {
    List<SystemTask> tasks = presenter.getTasks();
    SystemTask selected = presenter.getSelectedTask();

    // Populate ListElement items using Toolkit DSL
    ListElement<?> currentList = list().highlightColor(Color.CYAN).autoScroll();

    for (int i = 0; i < tasks.size(); i++) {
      SystemTask t = tasks.get(i);
      String prefix = (i == presenter.getSelectedIndex()) ? "▶ " : "  ";
      currentList.add(row(text(prefix + t.id()).cyan().bold(), text(" - " + t.title())));
    }

    return column(
        // Header
        panel(
                "Santa Soft — Quarkus + TamboUI (Java 25)",
                text("Interactive System Task Monitor").bold().cyan())
            .rounded()
            .length(3),

        // Main Content: Left Menu List + Right Detail Pane
        row(panel("Tasks", currentList).rounded().length(42), renderDetailPane(selected)).fill(),

        // Footer Status Bar Component
        StatusBarComponent.renderStatusBar(
            presenter.getConfig().backend(), presenter.getSelectedIndex(), tasks.size()));
    // Note: Built-in UP/DOWN navigation and 'q' quit are handled automatically by
    // ToolkitRunner/ListElement.
    // TODO: verify against TamboUI Javadoc the exact .onKeyEvent(...) lambda signature on Element
    // for custom space bar binding
  }

  private Element renderDetailPane(SystemTask task) {
    if (task == null) {
      return panel("Task Details", text("No task selected.").dim()).rounded();
    }

    return panel(
            "Task Details: " + task.id(),
            column(
                row(text("Title:    ").bold(), text(task.title())),
                row(text("Category: ").bold(), text(task.category()).yellow()),
                row(text("Status:   ").bold(), text(task.formattedStatus()).cyan()),
                spacer(),
                text("Completion Progress:").bold(),
                gauge(task.progressRatio()),
                spacer(),
                table()
                    .header("Metric", "Value")
                    .row("Task ID", task.id())
                    .row("Progress %", "%d%%".formatted((int) (task.progressRatio() * 100)))))
        .rounded();
  }
}
