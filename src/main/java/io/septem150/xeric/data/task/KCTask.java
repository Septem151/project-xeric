package io.septem150.xeric.data.task;

import io.septem150.xeric.data.KillCount;
import io.septem150.xeric.data.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class KCTask extends Task {
  private String boss;
  private int total;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    String name = boss;
    if ("Lunar Chest".equals(name)) {
      name += "s";
    } else if ("Hueycoatl".equals(name)) {
      name = "The " + name;
    }
    for (KillCount killCount : playerInfo.getKillCounts()) {
      if (killCount.getName().equals(name)) {
        return killCount.getCount() >= total;
      }
    }
    return false;
  }
}
