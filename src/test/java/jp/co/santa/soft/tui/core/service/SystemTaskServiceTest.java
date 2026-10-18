package jp.co.santa.soft.tui.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.buffer.Cell;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import jp.co.santa.soft.tui.core.model.SystemTask;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SystemTaskServiceTest {

  @Inject SystemTaskService service;

  @Test
  void shouldLoadInitialTasksAndFormatStatus() {
    List<SystemTask> tasks = service.findAllTasks();
    assertFalse(tasks.isEmpty());

    SystemTask first = service.findByIndex(0);
    assertNotNull(first);
    assertEquals("TSK-101", first.id());
    assertTrue(first.formattedStatus().contains("COMPLETED"));
  }

  @Test
  void shouldRenderCellToTamboUiBufferDirectly() {
    // Demonstrates headless TUI buffer testing without requiring a physical TTY
    Rect area = new Rect(0, 0, 20, 5);
    Buffer buffer = Buffer.empty(area);

    buffer.set(0, 0, new Cell("T", Style.EMPTY.fg(Color.CYAN)));
    Cell cell = buffer.get(0, 0);

    assertNotNull(cell);
  }
}
