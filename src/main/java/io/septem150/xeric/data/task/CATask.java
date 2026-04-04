package io.septem150.xeric.data.task;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.player.PlayerInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;

@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CATask extends Task {
  @SerializedName("count")
  private int total;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getCombatAchievements().size() >= total;
  }
}
