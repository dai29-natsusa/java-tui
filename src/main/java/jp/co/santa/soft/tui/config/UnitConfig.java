package jp.co.santa.soft.tui.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.Secret;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;
import java.util.List;
import java.util.Optional;
import jp.co.santa.soft.tui.config.converter.HeapConfig;
import jp.co.santa.soft.tui.config.converter.HeapMatrixConverter;

@ConfigMapping(prefix = "unit")
public interface UnitConfig {

  Optional<String> project();

  @WithDefault("0")
  int id();

  @WithDefault("unit0")
  String name();

  @WithDefault("192.168.10.10")
  String ip();

  @WithDefault("ol8unit0")
  String hostname();

  @WithDefault("/home/unit/WORKDIR")
  String workdir();

  List<User> users();

  List<Service> services();

  List<Instance> instances();

  @WithConverter(HeapMatrixConverter.class)
  HeapConfig[] memory();

  interface User {
    String username();

    Secret<String> password();

    Optional<Integer> userId();

    Optional<Integer> groupId();
  }

  interface Service {
    int id();

    String name();

    int pid();
  }

  interface Instance {
    String name();

    int pid();

    String location();
  }
}
