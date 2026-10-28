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
    assertEquals(2, unitConfig.units().size());

    var unit01 = "unit01";
    assertEquals("vsmc", unitConfig.units().get(unit01).project().orElseThrow());
    assertEquals(1, unitConfig.units().get(unit01).id());
    assertEquals("xmcs1", unitConfig.units().get(unit01).name());
    assertEquals("192.168.40.1", unitConfig.units().get(unit01).ip());
    assertEquals("ol8xmcs1", unitConfig.units().get(unit01).hostname());
    assertEquals("/home/xmcs/XMCS", unitConfig.units().get(unit01).workdir());
    assertEquals(7, unitConfig.units().get(unit01).heap().length);

    assertEquals(3, unitConfig.units().get(unit01).users().size());
    assertEquals("xmcs", unitConfig.units().get(unit01).users().getFirst().username());
    assertNotNull(unitConfig.units().get(unit01).users().getFirst().password());
    assertEquals(-1, unitConfig.units().get(unit01).users().getFirst().userId().orElseThrow());
    assertEquals(-1, unitConfig.units().get(unit01).users().getFirst().groupId().orElseThrow());
    assertEquals("oracle", unitConfig.units().get(unit01).users().get(1).username());
    assertNotNull(unitConfig.units().get(unit01).users().get(1).password());
    assertEquals(-1, unitConfig.units().get(unit01).users().get(1).userId().orElseThrow());
    assertEquals(-1, unitConfig.units().get(unit01).users().get(1).groupId().orElseThrow());
    assertEquals("root", unitConfig.units().get(unit01).users().getLast().username());
    assertNotNull(unitConfig.units().get(unit01).users().getLast().password());
    assertThrows(
        NoSuchElementException.class,
        () -> unitConfig.units().get(unit01).users().getLast().userId().orElseThrow());
    assertThrows(
        NoSuchElementException.class,
        () -> unitConfig.units().get(unit01).users().getLast().groupId().orElseThrow());

    assertEquals(1, unitConfig.units().get(unit01).services().size());
    assertEquals(1, unitConfig.units().get(unit01).services().getFirst().id());
    assertEquals("oracle", unitConfig.units().get(unit01).services().getFirst().name());
    assertEquals(999, unitConfig.units().get(unit01).services().getLast().pid());

    assertEquals(3, unitConfig.units().get(unit01).instances().size());
    assertEquals("xmcs_server", unitConfig.units().get(unit01).instances().getFirst().name());
    assertEquals(999, unitConfig.units().get(unit01).instances().getFirst().pid());
    assertEquals(
        "/home/xmcs/XMCS/xmcs_server/scripts/core.sh",
        unitConfig.units().get(unit01).instances().getFirst().location());
    assertEquals("amps_emulator", unitConfig.units().get(unit01).instances().get(1).name());
    assertEquals(888, unitConfig.units().get(unit01).instances().get(1).pid());
    assertEquals(
        "/home/xmcs/XMCS/amhs_emulator/scripts/emu.sh",
        unitConfig.units().get(unit01).instances().get(1).location());
    assertEquals("xmcs-monitor", unitConfig.units().get(unit01).instances().getLast().name());
    assertEquals(777, unitConfig.units().get(unit01).instances().getLast().pid());
    assertEquals(
        "/home/xmcs/XMCS/xmcs-monitor/scripts/monitor.sh",
        unitConfig.units().get(unit01).instances().getLast().location());

    var unit02 = "unit02";
    assertEquals("vsmc", unitConfig.units().get(unit02).project().orElseThrow());
    assertEquals(2, unitConfig.units().get(unit02).id());
    assertEquals("xmcs2", unitConfig.units().get(unit02).name());
    assertEquals("192.168.40.2", unitConfig.units().get(unit02).ip());
    assertEquals("ol8xmcs2", unitConfig.units().get(unit02).hostname());
    assertEquals("/home/xmcs/XMCS", unitConfig.units().get(unit02).workdir());
    assertEquals(7, unitConfig.units().get(unit02).heap().length);

    assertEquals(3, unitConfig.units().get(unit02).users().size());
    assertEquals("xmcs", unitConfig.units().get(unit02).users().getFirst().username());
    assertNotNull(unitConfig.units().get(unit02).users().getFirst().password());
    assertEquals(-1, unitConfig.units().get(unit02).users().getFirst().userId().orElseThrow());
    assertEquals(-1, unitConfig.units().get(unit02).users().getFirst().groupId().orElseThrow());
    assertEquals("oracle", unitConfig.units().get(unit02).users().get(1).username());
    assertNotNull(unitConfig.units().get(unit02).users().get(1).password());
    assertEquals(-1, unitConfig.units().get(unit02).users().get(1).userId().orElseThrow());
    assertEquals(-1, unitConfig.units().get(unit02).users().get(1).groupId().orElseThrow());
    assertEquals("root", unitConfig.units().get(unit02).users().getLast().username());
    assertNotNull(unitConfig.units().get(unit02).users().getLast().password());
    assertThrows(
        NoSuchElementException.class,
        () -> unitConfig.units().get(unit02).users().getLast().userId().orElseThrow());
    assertThrows(
        NoSuchElementException.class,
        () -> unitConfig.units().get(unit02).users().getLast().groupId().orElseThrow());

    assertEquals(1, unitConfig.units().get(unit02).services().size());
    assertEquals(1, unitConfig.units().get(unit02).services().getFirst().id());
    assertEquals("oracle", unitConfig.units().get(unit02).services().getFirst().name());
    assertEquals(999, unitConfig.units().get(unit02).services().getLast().pid());

    assertEquals(2, unitConfig.units().get(unit02).instances().size());
    assertEquals("xmcs_server", unitConfig.units().get(unit02).instances().getFirst().name());
    assertEquals(999, unitConfig.units().get(unit02).instances().getFirst().pid());
    assertEquals(
        "/home/xmcs/XMCS/xmcs_server/scripts/core.sh",
        unitConfig.units().get(unit02).instances().getFirst().location());
    assertEquals("xmcs-monitor", unitConfig.units().get(unit02).instances().getLast().name());
    assertEquals(777, unitConfig.units().get(unit02).instances().getLast().pid());
    assertEquals(
        "/home/xmcs/XMCS/xmcs-monitor/scripts/monitor.sh",
        unitConfig.units().get(unit02).instances().getLast().location());
  }
}
