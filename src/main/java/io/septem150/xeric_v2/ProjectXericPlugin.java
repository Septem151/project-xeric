package io.septem150.xeric_v2;

import static java.lang.Math.round;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.hiscore.HiscoreStore;
import io.septem150.xeric.data.hiscore.LocalHiscoreStore;
import io.septem150.xeric.data.task.LocalTaskStore;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.data.task.TaskTypeAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
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
  @Inject
  private @Named("xericGson") Gson gson;

  @Inject
  private Client client;
  @Inject
  private ClientThread clientThread;
  @Inject
  private ScheduledExecutorService executor;
  @Inject
  private ProjectXericApiClient apiClient;

  public static final int COLLECTION_LOG_TRANSMIT_SCRIPT_ID = 4100;
  public static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;

  private int tickClogScript = -1;
  private int tickClogUpdated = -1;
  private boolean clogUpdated = false;
  // Collection Log ?
  private final Map<Integer, Integer> playerItems = new HashMap<>();
  private final String messageSender = "ProjectXeric";

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
//    panel = injector.getInstance(ProjectXericPanel.class);
//    manager.startUp();
//    panel.startUp();
//    SwingUtilities.invokeLater(panel::refresh);
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    clogUpdated = false;
//    panel.shutDown();
//    manager.shutDown();
  }

  @Subscribe
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
//      SwingUtilities.invokeLater(panel::refresh);
    }
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (event.getGameState() == GameState.LOGIN_SCREEN) {
      if (client.getLocalPlayer() != null) {
        new Thread(this::updateProfileAsync).start();
      }
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    int tick = client.getTickCount();
    boolean clogScriptFired = tickClogScript != -1;
    int clogScriptBuffer = 2;
    boolean clogScriptFinished = tickClogScript + clogScriptBuffer < tick;
    if (clogScriptFired && clogScriptFinished) {
      tickClogScript = -1;
      executor.execute(this::updateProfileAsync);
    }
  }

  /**
   * Handle the Collection Log transmission script that has arguments for an item id and the
   * quantity of said item that the player has.
   *
   * @param event ScriptPreFired event, could be any script, args include parameters to the script.
   */
  @Subscribe
  public void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() == COLLECTION_LOG_TRANSMIT_SCRIPT_ID) {
      tickClogScript = client.getTickCount();

      Object[] args = event.getScriptEvent().getArguments();
      int itemId = (int) args[1];
      int quantity = (int) args[2];

      playerItems.put(itemId, quantity);
    }
  }

  /**
   * Handle the Collection Log script to then fire off a transmission script per item in the clog,
   * that is then handled by the script pre-fire subscription.
   *
   * @param event ScriptPostFired event, could be any script, results stored in int stack.
   */
  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() == COLLECTION_LOG_SETUP_SCRIPT_ID) {
      if (clogUpdated) {
        return;
      }
      int clogUpdatedBuffer = 50;
      if (tickClogUpdated != -1 && tickClogUpdated + clogUpdatedBuffer > client.getTickCount()) {
        client.addChatMessage(ChatMessageType.CONSOLE, messageSender,
            "Last update within " + round(clogUpdatedBuffer * 0.6)
                + " seconds. You can update again in " + round(
                (tickClogUpdated + clogUpdatedBuffer - client.getTickCount()) * 0.6) + " seconds.",
            messageSender);
        return;
      }
      tickClogUpdated = client.getTickCount();
      clogUpdated = true;

      client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
      client.runScript(2240);
      client.addChatMessage(ChatMessageType.CONSOLE, messageSender, "Updating your clan rank progress...",
          messageSender);
    }
  }

  private void updateProfileAsync() {
    if (!isValidRequest()) {
      return;
    }
    fetchProfileAsync().thenCompose((data) -> apiClient.updateProfileAsync(data))
        .whenComplete((result, exc) -> {
          if (exc != null) {
            String errorMessage = "Unable to update your clan rank progress.";
            clientThread.invokeLater(() -> {
              client.addChatMessage(ChatMessageType.CONSOLE, messageSender, errorMessage,
                  messageSender);
            });
            throw new RuntimeException(errorMessage);
          }
          clientThread.invokeLater(() -> {
            client.addChatMessage(ChatMessageType.CONSOLE, messageSender,
                "Your clan rank progress has been updated.",
                messageSender);
          });
        });
  }

  private boolean isValidRequest() {
    if (!isValidProfileType()) {
      return false;
    }
    long accountHash = client.getAccountHash();
    if (accountHash == -1) {
      return false;
    }
    Player player = client.getLocalPlayer();
    return player != null && player.getName() != null;
  }

  private boolean isValidProfileType() {
    RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
    return RuneScapeProfileType.STANDARD.equals(profileType);
  }

  private CompletableFuture<PlayerData> fetchProfileAsync() {
    CompletableFuture<PlayerData> playerDataFuture = new CompletableFuture<>();
    clientThread.invokeLater(() -> {
      PlayerData playerData = new PlayerData();
      Player player = client.getLocalPlayer();
      String username = player.getName();

      playerData.setUsername(username);
      playerData.setAccountType(client.getVarbitValue(VarbitID.IRONMAN));
      playerData.setClan(getPlayerClanData(player));
      for (Skill skill : Skill.values()) {
        String name = skill.getName();
        int xp = client.getSkillExperience(skill);
        playerData.getSkills().put(name, xp);
      }
      for (Quest quest : Quest.values()) {
        int id = quest.getId();
        QuestState stateEnum = quest.getState(client);
        int state = 0;
        if (stateEnum == QuestState.IN_PROGRESS) {
          state = 1;
        } else if (stateEnum == QuestState.FINISHED) {
          state = 2;
        }
        playerData.getQuests().put(id, state);
      }
      for (CombatAchievementTier tier : CombatAchievementTier.values()) {
        int id = tier.getId();
        int completedCount = tier.getCompletedCount(client);
        playerData.getCombatAchievementTiers().put(id, completedCount);
      }
      for (AchievementDiary diary : AchievementDiary.values()) {
        int areaId = diary.getId();
        int[] completedCounts = diary.getTiersCompletedCount(client);
        for (int tierIndex = 0; tierIndex < completedCounts.length; tierIndex++) {
          int completedCount = completedCounts[tierIndex];
          playerData.getAchievementDiaryTiers()
              .add(new AchievementDiaryTierData(areaId, tierIndex, completedCount));
        }
      }
      playerData.setItems(playerItems);
      log.debug(playerData.toString());
      playerDataFuture.complete(playerData);
    });
    return playerDataFuture;
  }

  private PlayerClanData getPlayerClanData(Player player) {
    ClanSettings clanSettings = client.getClanSettings();
    if (clanSettings == null) {
      return null;
    }
    ClanMember member = clanSettings.findMember(player.getName());
    if (member == null) {
      return null;
    }
    ClanRank rank = member.getRank();
    if (rank == null) {
      return null;
    }
    ClanTitle title = clanSettings.titleForRank(rank);
    if (title == null) {
      return null;
    }
    return new PlayerClanData(clanSettings.getName(), rank.getRank(), title.getId(),
        title.getName());
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
