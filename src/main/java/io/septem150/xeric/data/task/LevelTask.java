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
          playerInfo.getLevels().stream().map(Level::getValue).mapToInt(Integer::intValue).sum();
    } else {
      for (Level level : playerInfo.getLevels()) {
        if (level.getName().equalsIgnoreCase(this.level)) {
          playerLevel = level.getValue();
          break;
        }
      }
    }
    return playerLevel >= goal;
  }
}
