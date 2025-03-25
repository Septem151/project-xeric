package io.septem150.xeric;

import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.panel.ProjectXericPanel;
import io.septem150.xeric.player.PlayerManager;
import io.septem150.xeric.task.LocalTaskStore;
import io.septem150.xeric.task.TaskStore;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
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
  @Inject private ClientThread clientThread;
  @Inject private ProjectXericConfig config;
  @Inject private PlayerManager playerManager;

  private ProjectXericPanel panel;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    panel.init();
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    panel.stop();
    playerManager.unloadPlayer();
  }

  /**
   * Action to perform on GameStateChanged event.
   *
   * @param gameStateChanged GameStateChanged the propagated event
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged gameStateChanged) {
    switch (gameStateChanged.getGameState()) {
      case LOGGED_IN:
        // Invoke later so player data can finish loading in client
        clientThread.invokeLater(
            () -> {
              boolean loaded = playerManager.loadPlayer();
              if (loaded) {
                panel.reload();
              }
              return loaded;
            });
        break;
      case LOGIN_SCREEN:
        playerManager.unloadPlayer();
        panel.reload();
      default:
        return;
    }
  }

  @Subscribe
  public void onGameTick(GameTick gameTick) {}

  @Override
  public void configure(Binder binder) {
    binder.bind(TaskStore.class).to(LocalTaskStore.class);
    super.configure(binder);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }
}
