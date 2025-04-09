package io.septem150.xeric.data;

import io.septem150.xeric.data.task.Task;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.NonNull;

@Data
@Singleton
public class PlayerInfo {
  private String username;
  private AccountType accountType;
  private boolean slayerException;
  private @NonNull List<QuestProgress> quests;
  private @NonNull List<DiaryProgress> diaries;
  private @NonNull List<Level> levels;
  private @NonNull List<KillCount> killCounts;
  private @NonNull List<CombatAchievement> combatAchievements;
  private @NonNull CollectionLog collectionLog;
  private @NonNull List<Task> tasks;

  @Inject
  private PlayerInfo() {
    clear();
  }

  public void clear() {
    username = null;
    accountType = null;
    slayerException = false;
    quests = new ArrayList<>();
    diaries = new ArrayList<>();
    levels = new ArrayList<>();
    killCounts = new ArrayList<>();
    collectionLog = new CollectionLog();
    tasks = new ArrayList<>();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(tasks.stream().mapToInt(Task::getTier).sum());
  }
}
