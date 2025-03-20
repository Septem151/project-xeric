package io.septem150.xeric;

import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.task.InMemoryTaskStore;
import io.septem150.xeric.task.TaskManager;
import io.septem150.xeric.task.TaskStore;
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

/**
 * Project Xeric plugin.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@Slf4j
@PluginDescriptor(name = "Project Xeric")
public class ProjectXericPlugin extends Plugin {
  @Inject private Client client;
  @Inject private ProjectXericConfig config;
  @Inject private TaskManager taskManager;

  @Override
  public void configure(Binder binder) {
    super.configure(binder);
    binder.bind(TaskStore.class).to(InMemoryTaskStore.class);
  }

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    taskManager.getAllTasks().forEach(task -> log.info(task.toString()));
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
