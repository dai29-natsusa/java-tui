package jp.co.santa.soft.core.ssh;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SshServiceTest {
  private final String storeKeyFilesPath = "./config/id_ed25519";

  @Inject SshService sshService;

  @BeforeEach
  void setup() throws IOException {
    Files.deleteIfExists(Paths.get(storeKeyFilesPath));
  }

  @Test
  void givenPathAndComment_whenGenerateSshKeys_thenFoundKeyFilesOnGivenPath() throws IOException {
    await()
        .atMost(Duration.of(5, SECONDS))
        .pollInterval(Duration.of(100, MILLIS))
        .until(() -> Files.notExists(Paths.get(storeKeyFilesPath)));

    Path pathPri = Paths.get(storeKeyFilesPath);
    Path pathPub = Paths.get(storeKeyFilesPath + ".pub");
    assertDoesNotThrow(() -> sshService.generateSshKeys(pathPri, "testing"));
    assertTrue(pathPri.toFile().exists());
    assertTrue(pathPub.toFile().exists());
    assertTrue(Files.readString(pathPub).trim().startsWith("ssh-ed25519"));
    assertTrue(Files.readString(pathPub).trim().endsWith("testing"));
  }

  @Test
  void givenPathAndCommentNull_whenGenerateSshKeys_thenStillFoundKeyFilesOnGivenPath()
      throws IOException {
    await()
        .atMost(Duration.of(5, SECONDS))
        .pollInterval(Duration.of(100, MILLIS))
        .until(() -> Files.notExists(Paths.get(storeKeyFilesPath)));

    Path pathPri = Path.of(storeKeyFilesPath);
    Path pathPub = Path.of(storeKeyFilesPath + ".pub");
    assertDoesNotThrow(() -> sshService.generateSshKeys(pathPri, null));
    assertTrue(pathPri.toFile().exists());
    assertTrue(pathPub.toFile().exists());
    assertTrue(Files.readString(pathPub).trim().startsWith("ssh-ed25519"));
  }

  @Test
  void given_when_then() {
    Path privatePath = Path.of(storeKeyFilesPath);
    Path publicPath = Path.of(storeKeyFilesPath + ".pub");
    String privateKey =
        """
    -----BEGIN PRIVATE KEY-----
    MC4CAQ_With_Dummy
    -----END PRIVATE KEY-----
    """;
    String publicKey =
        """
    ssh-ed25519 AAAAC3_With_Dummy testing
    """;

    try {
      Files.writeString(privatePath, privateKey);
      Files.writeString(publicPath, publicKey);
    } catch (IOException _) {
    }

    await()
        .atMost(Duration.of(5, SECONDS))
        .pollInterval(Duration.of(100, MILLIS))
        .until(() -> Files.exists(Paths.get(storeKeyFilesPath)));

    assertDoesNotThrow(() -> sshService.generateSshKeys(Paths.get(storeKeyFilesPath), "testing"));
  }
}
