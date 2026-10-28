package jp.co.santa.soft.tui.config.state;

import io.smallrye.config.Secret;
import java.util.Optional;

public record UserRecord(
    String username,
    Secret<String> password,
    Optional<Integer> userId,
    Optional<Integer> groupId) {}
