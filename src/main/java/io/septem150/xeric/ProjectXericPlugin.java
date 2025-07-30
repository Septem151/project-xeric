package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.data.hiscore.HiscoreStore;
import io.septem150.xeric.data.hiscore.LocalHiscoreStore;
import io.septem150.xeric.data.task.LocalTaskStore;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.data.task.TaskTypeAdapter;
import io.septem150.xeric.event.PanelUpdate;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameTick;
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
@PluginDescriptor(name = ProjectXericConfig.NAME)
public final class ProjectXericPlugin extends Plugin {
  @Inject
  private @Named("xericGson") Gson gson;

  @Inject private ConfigManager configManager;
  @Inject private ProjectXericManager manager;
  @Inject private Client client;

  private ProjectXericPanel panel;
  private int updatePanel;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    manager.startUp();
    panel.startUp();
    SwingUtilities.invokeLater(panel::refresh);
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    panel.shutDown();
    manager.shutDown();
  }

  @Subscribe
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      // ::xeric - resets your saved collection log data and refreshes the side panel
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.CLOG_DATA_KEY);
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_DATA_KEY);
      manager.getPlayerInfo().clear();
      manager.shutDown();
      manager.startUp();
      SwingUtilities.invokeLater(panel::refresh);
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (updatePanel > 0 && --updatePanel == 0) {
      SwingUtilities.invokeLater(panel::refresh);
    }
  }

  @Subscribe
  public void onPanelUpdate(PanelUpdate event) {
    if (updatePanel <= 0) updatePanel = 3;
  }

  @Override
  public void configure(Binder binder) {
    super.configure(binder);
    binder.bind(TaskStore.class).to(LocalTaskStore.class);
    binder.bind(HiscoreStore.class).to(LocalHiscoreStore.class);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  public Gson provideGson(Gson gson) {
    return gson.newBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(Task.class, new TaskTypeAdapter())
        .create();
  }
}
