package io.septem150.xeric;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Project Xeric plugin configuration.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@ConfigGroup(ProjectXericConfig.GROUP)
public interface ProjectXericConfig extends Config {
  String GROUP = "projectxeric";

  /**
   * The message to show to the user when they login.
   *
   * @return String a greeting
   */
  @ConfigItem(
      keyName = "greeting",
      name = "Welcome Greeting",
      description = "The message to show to the user when they login")
  default String greeting() {
    return "Hello";
  }
}
