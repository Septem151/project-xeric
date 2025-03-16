package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.inject.Provides;
import io.septem150.xeric.task.Task;
import io.septem150.xeric.task.TaskManager;
import io.septem150.xeric.task.TaskTypeAdapter;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.RuneLiteAPI;

/**
 * Project Xeric plugin.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@Slf4j
@PluginDescriptor(name = "Project Xeric")
public class ProjectXericPlugin extends Plugin {

  public static final Gson GSON =
      RuneLiteAPI.GSON
          .newBuilder()
          .registerTypeHierarchyAdapter(Task.class, new TaskTypeAdapter())
          .create();
  @Inject private Client client;
  @Inject private ProjectXericConfig config;
  @Inject private TaskManager taskManager;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    taskManager.init();
    System.out.println(GSON.toJson(taskManager.getTasks()));
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
  }

  /**
   * Action to perform on GameStateChanged event.
   *
   * @param gameStateChanged GameStateChanged the propagated event
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged gameStateChanged) {
    if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
      client.addChatMessage(
          ChatMessageType.GAMEMESSAGE, "", "Project Xeric says " + config.greeting(), null);
    }
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }
}
