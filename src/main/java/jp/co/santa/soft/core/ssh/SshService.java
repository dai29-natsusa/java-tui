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
import java.util.Set;

@ApplicationScoped
@RegisterForReflection
public class SshService {

  /**
   * Generates an SSH Key Pair and saves private/public files.
   *
   * @param keysPath Path to save private key file (e.g. ~/.ssh/id_ed25519)
   * @param comment Comment appended to the end of public key (e.g. "admin@server")
   */
  public void generateSshKeys(final String keysPath, final String comment)
      throws NoSuchAlgorithmException, IOException {
    String defaultAlgorithm = "Ed25519";
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(defaultAlgorithm);

    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    // 1. Private PEM
    String privatePem = encodePrivateKeyToPem(keyPair);
    Path keyPath = Paths.get(keysPath);
    Files.writeString(keyPath, privatePem, StandardCharsets.UTF_8);

    // Enforce strict SSH file permissions (chmod 600) on Linux/Unix
    setStrictFilePermissions(keyPath);

    // 2. Public PEM
    Path publicPem = Paths.get(keyPath + ".pub");
    String publicOpenSsh = encodePublicKeyToOpenSsh(keyPair, comment);
    Files.writeString(publicPem, publicOpenSsh, StandardCharsets.UTF_8);
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
