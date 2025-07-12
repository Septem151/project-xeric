package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.Level;
import io.septem150.xeric.data.player.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.text.WordUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class LevelTask extends Task {
  private String level;
  private int goal;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    switch (level.toLowerCase()) {
      case "total":
        int total = playerInfo.getLevels().values().stream().mapToInt(Level::getAmount).sum();
        return total >= goal;
      case "any":
        for (Level level : playerInfo.getLevels().values()) {
          int currentExp = level.getExp();
          if (currentExp >= goal) {
            return true;
          }
        }
        return false;
      case "maxed":
        int maxed = 0;
        for (Level level : playerInfo.getLevels().values()) {
          if (level.getAmount() >= 99) {
            maxed++;
          }
        }
        return maxed >= goal;
      default:
        Level playerLevel = playerInfo.getLevels().getOrDefault(WordUtils.capitalize(level), null);
        if (playerLevel == null) return false;
        return playerLevel.getAmount() >= goal;
    }
  }
}
