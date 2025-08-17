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
  private Integer hiscoresId;
  private String username;
  private AccountType accountType;
  private @NonNull List<AccountException> accountExceptions = new ArrayList<>();
  //  private boolean slayerException;
  private @NonNull List<QuestProgress> quests = new ArrayList<>();
  private @NonNull List<DiaryProgress> diaries = new ArrayList<>();
  private @NonNull Map<String, Level> levels = new HashMap<>();
  private @NonNull Map<String, KillCount> killCounts = new HashMap<>();
  private @NonNull List<CombatAchievement> combatAchievements = new ArrayList<>();
  private @NonNull CollectionLog collectionLog = new CollectionLog();
  private @NonNull List<Task> tasks = new ArrayList<>();

  public PlayerInfo() {
    clear();
  }

  public void clear() {
    hiscoresId = null;
    username = null;
    accountType = null;
    accountExceptions.clear();
    //    slayerException = false;
    quests = new ArrayList<>();
    diaries = new ArrayList<>();
    levels = new HashMap<>();
    killCounts = new HashMap<>();
    combatAchievements = new ArrayList<>();
    collectionLog = new CollectionLog();
    tasks = new ArrayList<>();
  }

  public void addAccountException(AccountException accountException) {
    if (!accountExceptions.contains(accountException)) {
      accountExceptions.add(accountException);
    }
  }

  public void removeAccountException(AccountException accountException) {
    accountExceptions.remove(accountException);
  }

  public boolean hasAccountException(AccountException accountException) {
    return accountExceptions.contains(accountException);
  }

  public void setAccountException(AccountException accountException, boolean hasException) {
    if (hasException) {
      addAccountException(accountException);
    } else {
      removeAccountException(accountException);
    }
  }

  public int getPoints() {
    return tasks.stream().mapToInt(Task::getTier).sum();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(getPoints());
  }
}
