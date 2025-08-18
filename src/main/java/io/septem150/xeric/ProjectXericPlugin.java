package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.data.ProjectXericManager;
import io.septem150.xeric.data.task.CATask;
import io.septem150.xeric.data.task.CollectTask;
import io.septem150.xeric.data.task.DiaryTask;
import io.septem150.xeric.data.task.KCTask;
import io.septem150.xeric.data.task.LevelTask;
import io.septem150.xeric.data.task.QuestTask;
import io.septem150.xeric.data.task.RemoteTaskStore;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.panel.PanelUpdate;
import io.septem150.xeric.panel.ProjectXericPanel;
import io.septem150.xeric.util.RuntimeTypeAdapterFactory;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameTick;
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
@PluginDescriptor(name = ProjectXericConfig.NAME)
public final class ProjectXericPlugin extends Plugin {
  @Inject
  private @Named("xericGson") Gson gson;

  @Inject private ConfigManager configManager;
  @Inject private ProjectXericManager manager;
  @Inject private EventBus eventBus;

  private ProjectXericPanel panel;
  private int updatePanel;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    eventBus.register(manager);
    eventBus.register(panel);
    manager.startUp();
    panel.startUp();
    SwingUtilities.invokeLater(panel::refresh);
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    eventBus.unregister(manager);
    eventBus.unregister(panel);
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
    binder.bind(TaskStore.class).to(RemoteTaskStore.class);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  public Gson provideGson(Gson gson) {
    RuntimeTypeAdapterFactory<Task> taskTypeAdapterFactory =
        RuntimeTypeAdapterFactory.of(Task.class, "type", true)
            .registerSubtype(CATask.class, CATask.CA_TASK_TYPE)
            .registerSubtype(CollectTask.class, CollectTask.COLLECT_TASK_TYPE)
            .registerSubtype(DiaryTask.class, DiaryTask.DIARY_TASK_TYPE)
            .registerSubtype(KCTask.class, KCTask.KC_TASK_TYPE)
            .registerSubtype(LevelTask.class, LevelTask.LEVEL_TASK_TYPE)
            .registerSubtype(QuestTask.class, QuestTask.QUEST_TASK_TYPE);

    return gson.newBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(taskTypeAdapterFactory)
        .create();
  }
}
