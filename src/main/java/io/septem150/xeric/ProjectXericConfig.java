package io.septem150.xeric;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ProjectXericConfig.CONFIG_GROUP)
public interface ProjectXericConfig extends Config {
  String PLUGIN_NAME = "Project Xeric";
  String CONFIG_GROUP = "projectxeric";
  String CONFIG_KEY_CLOG = "clog";
  String CONFIG_KEY_TASKS = "tasks";
  String CONFIG_KEY_TASKS_HASH = "tasks-hash";
  String CONFIG_KEY_SLAYER = "slayer";
  String CONFIG_KEY_CHAT_MESSAGES = "chat_messages";

  @ConfigItem(
      keyName = CONFIG_KEY_SLAYER,
      name = "Slayer Exception",
      description = "Toggle on if you train Slayer off-island.")
  default boolean slayer() {
    return false;
  }

  @ConfigItem(
      keyName = CONFIG_KEY_CHAT_MESSAGES,
      name = "Task Completion Chat Messages",
      description = "Toggle on to receive chat messages upon completing tasks.")
  default boolean chatMessages() {
    return true;
  }
}
