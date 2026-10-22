package jp.co.santa.soft.core.ssh;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.EdECPublicKey;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

@ApplicationScoped
@RegisterForReflection
public class SshService {

  /**
   * Generates an SSH Key Pair and saves private/public files.
   *
   * @param keyPath Path to save private key file (e.g. ~/.ssh/id_ed25519)
   * @param comment Comment appended to the end of public key (e.g. "admin@server")
   */
  public void generateSshKeys(final Path keyPath, final String comment)
      throws NoSuchAlgorithmException, IOException {
    Path publicKeyPath = Paths.get(keyPath + ".pub");
    if (areBothKeysExisting(keyPath)) {
      return;
    } else {
      Files.deleteIfExists(keyPath);
      Files.deleteIfExists(publicKeyPath);
    }

    String defaultAlgorithm = "Ed25519";
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(defaultAlgorithm);

    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    // 1. Private PEM
    String privatePem = encodePrivateKeyToPem(keyPair);
    Files.writeString(keyPath, privatePem, StandardCharsets.UTF_8);

    // Enforce strict SSH file permissions (chmod 600) on Linux/Unix
    setStrictFilePermissions(keyPath);

    // 2. Public PEM
    String publicOpenSsh = encodePublicKeyToOpenSsh(keyPair, comment);
    Files.writeString(publicKeyPath, publicOpenSsh, StandardCharsets.UTF_8);
  }

  /** Checks if either the private or public key already exists at the target location. */
  public boolean areBothKeysExisting(final Path keyPath) {
    Path publicKeyPath = Path.of(keyPath + ".pub");
    return Files.exists(keyPath) && Files.exists(publicKeyPath);
  }

  /**
   * Connects to target server via SSH user/password, and registers local public key into remote authorized_keys.
   */
  public void registerKeyOnRemoteServer(String host, int port, String user, String password, Path publicKeyPath) throws Exception {
    if (Files.notExists(publicKeyPath)) {
      throw new IllegalArgumentException("Public key file does not exist at: " + publicKeyPath);
    }

    String pubKey = Files.readString(publicKeyPath, StandardCharsets.UTF_8).trim();

    try (SshClient client = SshClient.setUpDefaultClient()) {
      client.start();

      try (ClientSession session = client.connect(user, host, port)
              .verify(10, TimeUnit.SECONDS)
              .getSession()) {

        session.addPasswordIdentity(password);
        session.auth().verify(10, TimeUnit.SECONDS);


        // Bash script to safely inject key into ~/.ssh/authorized_keys
        String remoteCommand = String.format(
                "mkdir -p ~/.ssh && chmod 700 ~/.ssh && " +
                        "touch ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && " +
                        "grep -qF '%s' ~/.ssh/authorized_keys || echo '%s' >> ~/.ssh/authorized_keys",
                pubKey, pubKey
        );

        executeRemoteCommand(session, remoteCommand);
      } finally {
        client.stop();
      }
    }
  }

  public void executeRemoteCommand(ClientSession session, String command) throws Exception {
    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    try (var channel = session.createExecChannel(command)) {
      channel.setOut(responseStream);
      channel.setErr(responseStream);
      channel.open().verify(10, TimeUnit.SECONDS);

      channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(15));
    }
  }

  private String encodePublicKeyToOpenSsh(KeyPair keyPair, String comment) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream dataStream = new DataOutputStream(byteStream);

    EdECPublicKey edKey = (EdECPublicKey) keyPair.getPublic();
    byte[] rawBytes = edKey.getPoint().getY().toByteArray();
    dataStream.writeInt("ssh-ed25519".length());
    dataStream.write("ssh-ed25519".getBytes(StandardCharsets.UTF_8));
    dataStream.writeInt(rawBytes.length);
    dataStream.write(rawBytes);

    String encoded = Base64.getEncoder().encodeToString(byteStream.toByteArray());
    return "ssh-ed25519 " + encoded + " " + (comment != null ? comment : "");
  }

  private String encodePrivateKeyToPem(KeyPair keyPair) {
    String base64Key =
        Base64.getMimeEncoder(64, new byte[] {'\n'})
            .encodeToString(keyPair.getPrivate().getEncoded());
    return "-----BEGIN PRIVATE KEY-----\n" + base64Key + "\n-----END PRIVATE KEY-----\n";
  }

  private void setStrictFilePermissions(Path path) {
    try {
      Set<PosixFilePermission> perms =
          Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
      Files.setPosixFilePermissions(path, perms);
    } catch (UnsupportedOperationException e) {
      // Ignored on Windows environments where POSIX is not supported
    } catch (IOException e) {
      // Log warning if required
    }
  }
}
