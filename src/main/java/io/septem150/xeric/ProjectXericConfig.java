package io.septem150.xeric;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Project Xeric plugin configuration.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Carson Mullins</a>
 */
@ConfigGroup("projectxeric")
public interface ProjectXericConfig extends Config {

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
