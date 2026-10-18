package jp.co.santa.soft.tui.component;

import static dev.tamboui.toolkit.Toolkit.markupText;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;

import dev.tamboui.toolkit.element.Element;

/** Reusable UI component built with the TamboUI Toolkit DSL. */
public final class StatusBarComponent {

  private StatusBarComponent() {}

  public static Element renderStatusBar(String activeBackend, int selectedIndex, int totalItems) {
    return panel(
            "Status",
            row(
                markupText("Backend: [cyan]" + activeBackend + "[/]"),
                spacer(),
                text("Item " + (selectedIndex + 1) + " of " + totalItems).bold(),
                spacer(),
                text("Keys: [↑/↓] Navigate  [Space] Advance Task  [q] Quit").dim()))
        .rounded()
        .length(3);
  }
}
