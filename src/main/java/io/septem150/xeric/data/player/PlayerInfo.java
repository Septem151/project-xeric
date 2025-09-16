package io.septem150.xeric.data.player;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.ProjectXericManager;
import io.septem150.xeric.data.clog.ClogItem;
import io.septem150.xeric.data.clog.CollectionLog;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.diary.KourendDiary;
import io.septem150.xeric.data.task.Task;
import java.awt.Color;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
@Getter
public class PlayerInfo {
  @Getter(AccessLevel.NONE)
  @NonNull private final Client client;

  @Getter(AccessLevel.NONE)
  @NonNull private final ConfigManager configManager;

  @Getter(AccessLevel.NONE)
  @NonNull private final Gson gson;

  private boolean loggedIn;
  @Nullable private String username;
  @Nullable private AccountType accountType;

  @Setter private boolean slayerException;
  private final Map<Quest, QuestProgress> quests = new EnumMap<>(Quest.class);
  private final Map<KourendDiary, DiaryProgress> diaries = new EnumMap<>(KourendDiary.class);
  private final Map<Skill, Level> levels = new EnumMap<>(Skill.class);
  private final Map<String, KillCount> hiscores = new HashMap<>();
  private final Map<Integer, CombatAchievement> combatAchievements = new HashMap<>();
  private final CollectionLog collectionLog;
  private final Set<Task> allTasks = new HashSet<>();
  private final Set<Task> completedTasks = new HashSet<>();
  private final Set<Task> remainingTasks = new HashSet<>();
  private String tasksHash;

  @Inject
  public PlayerInfo(Client client, ConfigManager configManager, @Named("xericGson") Gson gson) {
    this.client = client;
    this.configManager = configManager;
    this.gson = gson;
    collectionLog = new CollectionLog(client, configManager, gson);
  }

  public void login(String username, AccountType accountType) {
    loggedIn = true;
    this.username = username;
    this.accountType = accountType;
  }

  public void logout() {
    loggedIn = false;
    // reset whether the clog interface has been opened on logout in case
    // the player leaves this client open and then obtains an item on another
    // client before logging in again to this one
    collectionLog.setInterfaceOpened(false);
    collectionLog.saveToRSProfile();
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP,
        ProjectXericConfig.TASKS_DATA_KEY,
        gson.toJson(completedTasks.stream().map(Task::getId).collect(Collectors.toSet())));
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_HASH_DATA_KEY, tasksHash);
  }

  public void reset() {
    loggedIn = false;
    username = null;
    accountType = null;
    slayerException = false;
    quests.clear();
    diaries.clear();
    levels.clear();
    hiscores.clear();
    combatAchievements.clear();
    collectionLog.reset();
    allTasks.clear();
    completedTasks.clear();
    remainingTasks.clear();
    tasksHash = null;
  }

  public void addClogItem(ClogItem item) {
    collectionLog.add(item);
  }

  public void addHiscore(String name, KillCount hiscore) {
    hiscores.put(name, hiscore);
  }

  public void addCombatAchievement(int id, CombatAchievement combatAchievement) {
    combatAchievements.put(id, combatAchievement);
  }

  public void addQuest(Quest quest, QuestProgress progress) {
    quests.put(quest, progress);
  }

  public boolean isClogInterfaceOpened() {
    return collectionLog.isInterfaceOpened();
  }

  public void setClogInterfaceOpened(boolean interfaceOpened) {
    collectionLog.setInterfaceOpened(interfaceOpened);
  }

  public Instant getClogLastUpdated() {
    return collectionLog.getLastUpdated();
  }

  public ImmutableSet<ClogItem> getClogItems() {
    return collectionLog.getItems();
  }

  public void loadClogFromRSProfile() {
    collectionLog.loadFromRSProfile();
  }

  public void loadTasksFromRSProfile() {
    completedTasks.clear();
    remainingTasks.clear();
    try {
      Type type = new TypeToken<Set<Integer>>() {}.getType();
      Set<Integer> taskIds =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_DATA_KEY),
              type);
      if (taskIds != null) {
        completedTasks.addAll(
            allTasks.stream()
                .filter(task -> taskIds.contains(task.getId()))
                .collect(Collectors.toSet()));
        remainingTasks.addAll(Sets.difference(allTasks, completedTasks));
      }
    } catch (JsonParseException err) {
      log.error("malformed task data in profile");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_DATA_KEY);
    }
  }

  private boolean isTaskListUpdated() {
    String prevTasksHash =
        configManager.getRSProfileConfiguration(
            ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_HASH_DATA_KEY);
    if (prevTasksHash == null || !prevTasksHash.equals(tasksHash)) {
      for (RuneScapeProfile rsProfile : configManager.getRSProfiles()) {
        String profileKey = rsProfile.getKey();
        configManager.unsetConfiguration(
            ProjectXericConfig.GROUP, profileKey, ProjectXericConfig.TASKS_DATA_KEY);
      }
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_HASH_DATA_KEY, tasksHash);

      return true;
    }
    return false;
  }

  public boolean checkForUpdatedTasks() {
    if (isTaskListUpdated()) {
      client.addChatMessage(
          ChatMessageType.GAMEMESSAGE,
          "",
          String.format(
              "[%s] Info: %s",
              ColorUtil.wrapWithColorTag(ProjectXericConfig.NAME, ProjectXericManager.DARK_GREEN),
              ColorUtil.wrapWithColorTag(
                  "Tasks have been updated! Check your tasks in the side panel.", Color.RED)),
          null);
      return true;
    }
    return false;
  }

  public void clearCompletedTasks() {
    completedTasks.clear();
    remainingTasks.clear();
    remainingTasks.addAll(Sets.difference(allTasks, completedTasks));
  }

  public void setAllTasks(Set<Task> tasks) {
    allTasks.clear();
    allTasks.addAll(tasks);
    try {
      tasksHash =
          String.format(
              "%032x",
              new BigInteger(
                  1,
                  MessageDigest.getInstance("MD5")
                      .digest(gson.toJson(allTasks).getBytes(StandardCharsets.UTF_8))));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public void addCompletedTask(Task task) {
    completedTasks.add(task);
    remainingTasks.remove(task);
  }

  public int getPoints() {
    return completedTasks.stream().mapToInt(Task::getTier).sum();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(getPoints());
  }
}
