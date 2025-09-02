package io.septem150.xeric;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.data.*;
import io.septem150.xeric.task.TaskBase;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;

@Slf4j
@Singleton
@Getter
public class PlayerData {
  @Getter(AccessLevel.NONE)
  @NonNull private final ConfigManager configManager;

  @Getter(AccessLevel.NONE)
  @NonNull private final Gson gson;

  private final CollectionLog collectionLog;
  private final Map<Integer, CombatAchievement> combatAchievements = new HashMap<>();
  private final Map<Skill, SkillLevel> levels = new EnumMap<>(Skill.class);
  private final Map<Quest, QuestProgress> quests = new EnumMap<>(Quest.class);

  private final Map<AchievementDiary, DiaryProgress> diaries =
      new EnumMap<>(AchievementDiary.class);

  private final Map<String, Hiscore> hiscores = new HashMap<>();
  private final Set<TaskBase> allTasks = new HashSet<>();
  private final Set<TaskBase> completedTasks = new HashSet<>();
  private final Set<TaskBase> remainingTasks = new HashSet<>();
  private String tasksHash;

  private boolean loggedIn;
  @Nullable private String username;
  @Nullable private AccountType accountType;
  @Setter private boolean slayerException;

  @Inject
  public PlayerData(Client client, ConfigManager configManager, @Named("xericGson") Gson gson) {
    this.configManager = configManager;
    this.gson = gson;
    collectionLog = new CollectionLog(client, configManager, gson);
  }

  public void reset() {
    loggedIn = false;
    username = null;
    accountType = null;
    slayerException = false;
    collectionLog.reset();
    combatAchievements.clear();
    levels.clear();
    quests.clear();
    diaries.clear();
    hiscores.clear();
    allTasks.clear();
    completedTasks.clear();
    remainingTasks.clear();
    tasksHash = null;
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
        ProjectXericConfig.CONFIG_GROUP,
        ProjectXericConfig.CONFIG_KEY_TASKS,
        gson.toJson(completedTasks.stream().map(TaskBase::getId).collect(Collectors.toSet())));
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS_HASH, tasksHash);
  }

  public void loadTasksFromRSProfile() {
    completedTasks.clear();
    remainingTasks.clear();
    try {
      Type type = new TypeToken<Set<Integer>>() {}.getType();
      Set<Integer> taskIds =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS),
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
          ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS);
    }
  }

  public boolean isTaskListUpdated() {
    String prevTasksHash =
        configManager.getRSProfileConfiguration(
            ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS_HASH);
    if (prevTasksHash == null || !prevTasksHash.equals(tasksHash)) {
      for (RuneScapeProfile rsProfile : configManager.getRSProfiles()) {
        String profileKey = rsProfile.getKey();
        configManager.unsetConfiguration(
            ProjectXericConfig.CONFIG_GROUP, profileKey, ProjectXericConfig.CONFIG_KEY_TASKS);
      }
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS_HASH, tasksHash);

      return true;
    }
    return false;
  }

  public void clearCompletedTasks() {
    completedTasks.clear();
    remainingTasks.clear();
    remainingTasks.addAll(Sets.difference(allTasks, completedTasks));
  }

  public int getPoints() {
    return completedTasks.stream().mapToInt(TaskBase::getTier).sum();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(getPoints());
  }

  public void setAllTasks(Set<TaskBase> tasks) {
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

  public void addCompletedTask(TaskBase task) {
    completedTasks.add(task);
    remainingTasks.remove(task);
  }
}
