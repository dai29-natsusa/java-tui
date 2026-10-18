package jp.co.santa.soft.tui.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "tamboui")
public interface TamboUiConfig {

  @WithDefault("panama")
  String backend();

  @WithDefault("/tcss/default.tcss")
  String themePath();

  @WithDefault("true")
  boolean mouseCapture();

  @WithDefault("50")
  long tickRateMs();
}
