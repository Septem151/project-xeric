package io.septem150.xeric.data.player;

import io.septem150.xeric.data.clog.CollectionLog;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.task.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

@Data
public class PlayerInfo {
  private String username;
  private AccountType accountType;
  private boolean slayerException;
  private @NonNull List<QuestProgress> quests;
  private @NonNull List<DiaryProgress> diaries;
  private @NonNull Map<String, Level> levels;
  private @NonNull Map<String, KillCount> killCounts;
  private @NonNull List<CombatAchievement> combatAchievements;
  private @NonNull CollectionLog collectionLog;
  private @NonNull List<Task> tasks;

  public PlayerInfo() {
    clear();
  }

  public void clear() {
    username = null;
    accountType = null;
    slayerException = false;
    quests = new ArrayList<>();
    diaries = new ArrayList<>();
    levels = new HashMap<>();
    killCounts = new HashMap<>();
    combatAchievements = new ArrayList<>();
    collectionLog = new CollectionLog();
    tasks = new ArrayList<>();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(tasks.stream().mapToInt(Task::getTier).sum());
  }
}
