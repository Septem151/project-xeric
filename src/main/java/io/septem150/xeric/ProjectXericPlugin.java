package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.data.task.LocalTaskStore;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.data.task.TaskTypeAdapter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
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
public final class ProjectXericPlugin extends Plugin {

  @Inject private EventBus eventBus;
  @Inject private ProjectXericConfig config;
  @Inject private ProjectXericManager manager;

  private ProjectXericPanel panel;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    panel.init();
    manager.init();
    SwingUtilities.invokeLater(panel::reload);
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    panel.stop();
    manager.clearPlayer();
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (event.getGameState() == GameState.LOGGED_IN) {
      SwingUtilities.invokeLater(panel::reload);
    }
  }

  @Subscribe
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      manager.clearRSProfile();
      SwingUtilities.invokeLater(panel::reload);
    }
  }

  @Subscribe
  public void onScriptPreFired(ScriptPreFired event) {}

  @Override
  public void configure(Binder binder) {
    super.configure(binder);
    binder.bind(TaskStore.class).to(LocalTaskStore.class);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  public Gson provideGson(Gson gson) {
    return gson.newBuilder().registerTypeAdapter(Task.class, new TaskTypeAdapter()).create();
  }
}
