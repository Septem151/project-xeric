package io.septem150.xeric.data.task;

import io.septem150.xeric.data.Level;
import io.septem150.xeric.data.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LevelTask extends Task {
  private String level;
  private int goal;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if ("total".equalsIgnoreCase(level)) {
      int total =
          playerInfo.getLevels().values().stream()
              .map(Level::getAmount)
              .mapToInt(Integer::intValue)
              .sum();
      return total >= goal;
    } else if ("any".equals(level)) {
      for (Level level : playerInfo.getLevels().values()) {
        int currentExp = level.getExp();
        if (currentExp >= goal) {
          return true;
        }
      }
      return false;
    } else if ("maxed".equals(level)) {
      int maxed = 0;
      for (Level level : playerInfo.getLevels().values()) {
        if (level.getAmount() >= 99) {
          maxed++;
        }
      }
      return maxed >= goal;
    } else {
      Level playerLevel = playerInfo.getLevels().getOrDefault(level, null);
      if (playerLevel == null) return false;
      return playerLevel.getAmount() >= goal;
    }
  }
}
