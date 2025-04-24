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
    int playerLevel = -1;
    if ("total".equalsIgnoreCase(level)) {
      playerLevel =
          playerInfo.getLevels().stream().map(Level::getAmount).mapToInt(Integer::intValue).sum();
      return playerLevel >= goal;
    } else if ("any".equals(level)) {
      for (Level level : playerInfo.getLevels()) {
        int currentExp = level.getExp();
        if (currentExp >= goal) {
          return true;
        }
      }
      return false;
    } else if ("maxed".equals(level)) {
      int maxed = 0;
      for (Level level : playerInfo.getLevels()) {
        if (level.getAmount() >= 99) {
          maxed++;
        }
      }
      return maxed >= goal;
    } else {
      for (Level level : playerInfo.getLevels()) {
        if (level.getName().equalsIgnoreCase(this.level)) {
          playerLevel = level.getAmount();
          break;
        }
      }
      return playerLevel >= goal;
    }
  }
}
