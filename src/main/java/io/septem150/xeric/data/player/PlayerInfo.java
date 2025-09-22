package io.septem150.xeric.data.player;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.clog.CollectionLog;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.task.Task;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;

@Slf4j
@Singleton
@Getter
@Setter
public class PlayerInfo {
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @NonNull private final ConfigManager configManager;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @NonNull private final Gson gson;

  @Setter(AccessLevel.NONE)
  @Nullable private String username;

  @Setter(AccessLevel.NONE)
  @Nullable private AccountType accountType;

  private boolean slayerException;
  @NonNull private List<QuestProgress> quests = new ArrayList<>();
  @NonNull private List<DiaryProgress> diaries = new ArrayList<>();
  @NonNull private Map<String, Level> levels = new HashMap<>();
  @NonNull private Map<String, KillCount> killCounts = new HashMap<>();
  @NonNull private List<CombatAchievement> combatAchievements = new ArrayList<>();
  @NonNull private CollectionLog collectionLog = new CollectionLog();
  @NonNull private Set<Task> allTasks = new HashSet<>();
  @NonNull private Set<Task> completedTasks = new HashSet<>();
  @NonNull private Set<Task> remainingTasks = new HashSet<>();
  @Nullable private String tasksHash;

  @Inject
  public PlayerInfo(ConfigManager configManager, @Named("xericGson") Gson gson) {
    this.configManager = configManager;
    this.gson = gson;
  }

  public void login(String username, AccountType accountType) {
    this.username = username;
    this.accountType = accountType;
  }

  public void logout() {
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP,
        ProjectXericConfig.TASKS_DATA_KEY,
        gson.toJson(completedTasks.stream().map(Task::getId).collect(Collectors.toSet())));
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_HASH_DATA_KEY, tasksHash);
  }

  public void reset() {
    username = null;
    accountType = null;
    slayerException = false;
    quests = new ArrayList<>();
    diaries = new ArrayList<>();
    levels = new HashMap<>();
    killCounts = new HashMap<>();
    combatAchievements = new ArrayList<>();
    collectionLog = new CollectionLog();
    allTasks = new HashSet<>();
    completedTasks = new HashSet<>();
    remainingTasks = new HashSet<>();
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

  public boolean isTaskListUpdated() {
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
