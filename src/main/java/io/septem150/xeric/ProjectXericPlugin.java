package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.data.ClogItem;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.StoredInfo;
import io.septem150.xeric.data.task.LocalTaskStore;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.data.task.TaskTypeAdapter;
import io.septem150.xeric.manager.CAManager;
import io.septem150.xeric.manager.ClogManager;
import io.septem150.xeric.manager.DiaryManager;
import io.septem150.xeric.manager.KCManager;
import io.septem150.xeric.manager.LevelManager;
import io.septem150.xeric.manager.PlayerManager;
import io.septem150.xeric.manager.QuestManager;
import io.septem150.xeric.manager.TaskManager;
import io.septem150.xeric.util.WorldUtil;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
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
public final class ProjectXericPlugin extends Plugin {
  @Inject private Client client;
  @Inject private ClientThread clientThread;
  @Inject private ProjectXericConfig config;
  @Inject private ConfigManager configManager;

  @Inject
  private @Named("xericGson") Gson gson;

  @Inject private PlayerInfo playerInfo;
  @Inject private CAManager caManager;
  @Inject private ClogManager clogManager;
  @Inject private DiaryManager diaryManager;
  @Inject private KCManager kcManager;
  @Inject private LevelManager levelManager;
  @Inject private PlayerManager playerManager;
  @Inject private QuestManager questManager;
  @Inject private TaskManager taskManager;

  private ProjectXericPanel panel;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    panel.init();
    caManager.startUp();
    clogManager.startUp();
    diaryManager.startUp();
    kcManager.startUp();
    levelManager.startUp();
    playerManager.startUp();
    questManager.startUp();
    taskManager.startUp();
    clientThread.invokeLater(
        () -> {
          if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client)) {
            load();
          }
        });
    SwingUtilities.invokeLater(panel::reload);
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    //    reset();
    //    clogItemIds = null;
    //    caTaskStructIds = null;
    panel.stop();
    caManager.shutDown();
    clogManager.shutDown();
    diaryManager.shutDown();
    kcManager.shutDown();
    levelManager.shutDown();
    playerManager.shutDown();
    questManager.shutDown();
    taskManager.shutDown();
    //    sessionManager.reset();
  }

  public void reset() {
    playerInfo.clear();
    caManager.reset();
    clogManager.reset();
    diaryManager.reset();
    kcManager.reset();
    levelManager.reset();
    playerManager.reset();
    questManager.reset();
    taskManager.reset();
  }

  public void load() {
    StoredInfo storedInfo = null;
    try {
      storedInfo =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY),
              StoredInfo.class);
    } catch (JsonSyntaxException exc) {
      log.warn("malformed stored data found, will ignore and overwrite.");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY);
    }
    if (storedInfo == null) {
      storedInfo = new StoredInfo();
      storedInfo.setSlayerException(config.slayer());
      storedInfo.setClogItems(new ArrayList<>());
      storedInfo.setTasks(new ArrayList<>());
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY, gson.toJson(storedInfo));
    }

    reset();
    clogManager.load(storedInfo);
    playerManager.load(storedInfo);
    taskManager.load(storedInfo);
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (event.getGameState() == GameState.LOGGED_IN
        && WorldUtil.isValidWorldType(client)
        && playerManager.isNewSession()) {
      load();
    } else if (event.getGameState() == GameState.LOGIN_SCREEN) {
      StoredInfo storedInfo = new StoredInfo();
      storedInfo.setSlayerException(playerInfo.isSlayerException());
      storedInfo.setClogItems(
          playerInfo.getCollectionLog().getItems().stream()
              .map(ClogItem::getId)
              .collect(Collectors.toList()));
      storedInfo.setTasks(
          playerInfo.getTasks().stream().map(Task::getId).collect(Collectors.toList()));
      storedInfo.setLastUpdated(playerInfo.getCollectionLog().getLastOpened());
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY, gson.toJson(storedInfo));
      reset();
    }
  }

  @Subscribe
  public void onPlayerUpdate(PlayerUpdate event) {
    event.getAction().accept(playerInfo);
    SwingUtilities.invokeLater(panel::reload);
  }

  @Subscribe
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY);
      reset();
      clientThread.invokeLater(
          () -> {
            if (client.getGameState() == GameState.LOGGED_IN
                && WorldUtil.isValidWorldType(client)) {
              load();
            }
          });
      SwingUtilities.invokeLater(panel::reload);
      client.addChatMessage(
          ChatMessageType.GAMEMESSAGE, "", "Your Hespori kill count is: 49.", null, true);
    }
  }

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
    return gson.newBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(Task.class, new TaskTypeAdapter())
        .create();
  }
}
