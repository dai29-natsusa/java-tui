package jp.co.santa.soft.tui.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.Secret;
import io.smallrye.config.WithDefault;
import java.util.List;

@ConfigMapping(prefix = "vsmc.unit")
public interface UnitConfig {

  @WithDefault("0")
  int id();

  @WithDefault("xmcs0")
  String name();

  @WithDefault("192.168.10.10")
  String ip();

  @WithDefault("ol8xmcs0")
  String hostname();

  @WithDefault("/home/xmcs/XMCS")
  String workdir();

  List<User> users();

  List<Service> services();

  List<Instance> instances();

  interface User {
    String username();

    Secret<String> password();
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
