package io.septem150.xeric.data.task;

import io.septem150.xeric.data.KillCount;
import io.septem150.xeric.data.player.PlayerInfo;
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
    KillCount kc = playerInfo.getKillCounts().getOrDefault(name, null);
    if (kc == null) return false;
    return kc.getCount() >= total;
  }
}
