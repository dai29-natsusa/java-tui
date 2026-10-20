package jp.co.santa.soft.tui.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UnitConfigTest {
  @Inject UnitConfig unitConfig;

  @Test
  void testUnitConfig() {
    assertEquals("vsmc", unitConfig.project().orElseThrow());
    assertEquals(1, unitConfig.id());
    assertEquals("xmcs1", unitConfig.name());
    assertEquals("192.168.40.1", unitConfig.ip());
    assertEquals("ol8xmcs1", unitConfig.hostname());
    assertEquals("/home/xmcs/XMCS", unitConfig.workdir());

    assertEquals(3, unitConfig.users().size());
    assertEquals("xmcs", unitConfig.users().getFirst().username());
    assertNotNull(unitConfig.users().getFirst().password());
    assertEquals(-1, unitConfig.users().getFirst().userId().orElseThrow());
    assertEquals(-1, unitConfig.users().getFirst().groupId().orElseThrow());
    assertEquals("oracle", unitConfig.users().get(1).username());
    assertNotNull(unitConfig.users().get(1).password());
    assertEquals(-1, unitConfig.users().get(1).userId().orElseThrow());
    assertEquals(-1, unitConfig.users().get(1).groupId().orElseThrow());
    assertEquals("root", unitConfig.users().getLast().username());
    assertNotNull(unitConfig.users().getLast().password());
    assertThrows(
        NoSuchElementException.class, () -> unitConfig.users().getLast().userId().orElseThrow());
    assertThrows(
        NoSuchElementException.class, () -> unitConfig.users().getLast().groupId().orElseThrow());

    assertEquals(1, unitConfig.services().size());
    assertEquals(1, unitConfig.services().getFirst().id());
    assertEquals("oracle", unitConfig.services().getFirst().name());
    assertEquals(999, unitConfig.services().getLast().pid());

    assertEquals(3, unitConfig.instances().size());
    assertEquals("xmcs_server", unitConfig.instances().getFirst().name());
    assertEquals(999, unitConfig.instances().getFirst().pid());
    assertEquals(
        "/home/xmcs/XMCS/xmcs_server/scripts/core.sh",
        unitConfig.instances().getFirst().location());
    assertEquals("amps_emulator", unitConfig.instances().get(1).name());
    assertEquals(888, unitConfig.instances().get(1).pid());
    assertEquals(
        "/home/xmcs/XMCS/amhs_emulator/scripts/emu.sh", unitConfig.instances().get(1).location());
    assertEquals("xmcs-monitor", unitConfig.instances().getLast().name());
    assertEquals(777, unitConfig.instances().getLast().pid());
    assertEquals(
        "/home/xmcs/XMCS/xmcs-monitor/scripts/monitor.sh",
        unitConfig.instances().getLast().location());
  }
}
