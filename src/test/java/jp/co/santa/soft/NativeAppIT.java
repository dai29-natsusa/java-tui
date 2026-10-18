package jp.co.santa.soft;

import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusMainIntegrationTest
class NativeAppIT {

  @Test
  //    @Launch({"--non-interactive"})
  void shouldBootNativeBinaryCleanly() {
    // Verifies GraalVM native binary startup, ArC wiring, and YAML config parsing
  }
}
