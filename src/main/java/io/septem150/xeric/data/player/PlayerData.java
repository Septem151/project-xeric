package io.septem150.xeric.data.player;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.CombatAchievement;
import io.septem150.xeric.data.Hiscore;
import io.septem150.xeric.data.QuestProgress;
import io.septem150.xeric.data.SkillLevel;
import io.septem150.xeric.data.clog.CollectionLog;
import io.septem150.xeric.data.diary.AchievementDiary;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.task.Task;
import java.lang.reflect.Type;
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
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
@Getter
public class PlayerData {
  @Getter(AccessLevel.NONE)
  @NonNull private final ConfigManager configManager;

  @Getter(AccessLevel.NONE)
  @NonNull private final Gson gson;

  @NonNull private final CollectionLog collectionLog;

  private final Map<Integer, CombatAchievement> combatAchievements = new HashMap<>();
  private final Map<Skill, SkillLevel> levels = new EnumMap<>(Skill.class);
  private final Map<Quest, QuestProgress> quests = new EnumMap<>(Quest.class);

  private final Map<AchievementDiary, DiaryProgress> diaries =
      new EnumMap<>(AchievementDiary.class);

  private final Map<String, Hiscore> hiscores = new HashMap<>();
  private final Set<Task> tasks = new HashSet<>();

  private boolean loggedIn;
  @Nullable private String username;
  @Nullable private AccountType accountType;
  @Setter private boolean slayerException;

  @Inject
  public PlayerData(
      ConfigManager configManager, @Named("xericGson") Gson gson, CollectionLog collectionLog) {
    this.configManager = configManager;
    this.gson = gson;
    this.collectionLog = collectionLog;
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
    tasks.clear();
  }

  public void login(String username, AccountType accountType) {
    loggedIn = true;
    this.username = username;
    this.accountType = accountType;
  }

  public void logout() {
    loggedIn = false;
  }

  public void saveTasksToRSProfile() {
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.CONFIG_GROUP,
        ProjectXericConfig.CONFIG_KEY_TASKS,
        gson.toJson(tasks.stream().map(Task::getId).collect(Collectors.toSet())));
  }

  public void loadTasksFromRSProfile(@NonNull Map<Integer, Task> allTasks) {
    try {
      Type type = new TypeToken<Set<Integer>>() {}.getType();
      Set<Integer> taskIds =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS),
              type);
      if (taskIds != null) {
        tasks.addAll(taskIds.stream().map(allTasks::get).collect(Collectors.toSet()));
      }
    } catch (JsonParseException err) {
      log.error("malformed task data in profile");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_TASKS);
    }
  }

  public int getPoints() {
    return tasks.stream().mapToInt(Task::getTier).sum();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(getPoints());
  }
}
