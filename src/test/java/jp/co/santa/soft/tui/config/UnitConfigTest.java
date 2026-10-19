package jp.co.santa.soft.tui.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UnitConfigTest {
  @Inject UnitConfig unitConfig;

  @Test
  void testUnitConfig() {
    assertEquals(1, unitConfig.id());
    assertEquals("xmcs1", unitConfig.name());
    assertEquals("192.168.10.10", unitConfig.ip());
    assertEquals("ol8xmcs1", unitConfig.hostname());
    assertEquals("/home/xmcs/XMCS", unitConfig.workdir());
    assertEquals(3, unitConfig.users().size());
    assertEquals(1, unitConfig.services().size());
    assertEquals(3, unitConfig.instances().size());
  }
}
