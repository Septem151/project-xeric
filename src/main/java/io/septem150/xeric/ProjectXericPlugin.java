package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.inject.Provides;
import io.septem150.xeric.data.ProjectXericManager;
import io.septem150.xeric.data.task.CATask;
import io.septem150.xeric.data.task.CollectTask;
import io.septem150.xeric.data.task.DiaryTask;
import io.septem150.xeric.data.task.KCTask;
import io.septem150.xeric.data.task.LevelTask;
import io.septem150.xeric.data.task.QuestTask;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskService;
import io.septem150.xeric.data.task.TaskType;
import io.septem150.xeric.panel.ProjectXericPanel;
import io.septem150.xeric.util.ImageService;
import io.septem150.xeric.util.RuntimeTypeAdapterFactory;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;
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
  @Inject private ConfigManager configManager;
  @Inject private ProjectXericManager manager;
  @Inject private EventBus eventBus;
  @Inject private TaskService taskService;
  @Inject private ImageService imageService;

  private ProjectXericPanel panel;

  @Override
  protected void startUp() throws Exception {
    panel = injector.getInstance(ProjectXericPanel.class);
    eventBus.register(manager);
    manager.startUp(panel);
  }

  @Override
  protected void shutDown() throws Exception {
    manager.shutDown();
    eventBus.unregister(manager);
  }

  @Subscribe
  void onCommandExecuted(CommandExecuted event) {
    // ::xeric - resets the plugin's saved data for all RS profiles then reloads
    if (event.getCommand().equals("xeric")) {
      for (RuneScapeProfile rsProfile : configManager.getRSProfiles()) {
        String profileKey = rsProfile.getKey();
        configManager.unsetConfiguration(
            ProjectXericConfig.GROUP, profileKey, ProjectXericConfig.TASKS_DATA_KEY);
        configManager.unsetConfiguration(
            ProjectXericConfig.GROUP, profileKey, ProjectXericConfig.CLOG_DATA_KEY);
        configManager.unsetConfiguration(
            ProjectXericConfig.GROUP, profileKey, ProjectXericConfig.TASKS_HASH_DATA_KEY);
      }
      taskService.deleteCache();
      imageService.clearCache();
      try {
        shutDown();
        startUp();
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  Gson provideGson(Gson gson) {
    RuntimeTypeAdapterFactory<Task> taskTypeAdapterFactory =
        RuntimeTypeAdapterFactory.of(Task.class, "type", true)
            .registerSubtype(CATask.class, TaskType.CA.getName())
            .registerSubtype(CollectTask.class, TaskType.CLOG.getName())
            .registerSubtype(DiaryTask.class, TaskType.DIARY.getName())
            .registerSubtype(KCTask.class, TaskType.HISCORE.getName())
            .registerSubtype(LevelTask.class, TaskType.LEVEL.getName())
            .registerSubtype(QuestTask.class, TaskType.QUEST.getName());

    return gson.newBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(taskTypeAdapterFactory)
        .create();
  }
}
