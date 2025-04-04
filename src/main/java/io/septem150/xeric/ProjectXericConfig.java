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
  String DATA_KEY = "clog";
  String SLAYER = "slayer";

  @ConfigItem(
      keyName = SLAYER,
      name = "Slayer Exception",
      description = "Toggle on if you train Slayer off-island.")
  default boolean slayer() {
    return false;
  }
}
