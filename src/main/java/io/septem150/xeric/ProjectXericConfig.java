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
  String NAME = "Project Xeric";
  String GROUP = "projectxeric";
  String CLOG_DATA_KEY = "clog";
  String TASKS_DATA_KEY = "tasks";
  String SLAYER = "slayer";
  String CHAT_MESSAGES = "chat_messages";

  @ConfigItem(
      keyName = SLAYER,
      name = "Slayer Exception",
      description = "Toggle on if you train Slayer off-island.")
  default boolean slayer() {
    return false;
  }

  @ConfigItem(
      keyName = CHAT_MESSAGES,
      name = "Task Completion Chat Messages",
      description = "Toggle on to receive chat messages upon completing tasks.")
  default boolean chatMessages() {
    return true;
  }
}
