package io.septem150.xeric.data.task;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.player.KillCount;
import io.septem150.xeric.data.player.PlayerInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreSkill;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class KCTask extends Task {
  @Getter
  @SerializedName("detail")
  private String boss;

  @SerializedName("count")
  private int total;

  private transient String fixedBoss;

  public static String fixBossName(String bossName) {
    // handle special cases where hiscore name doesn't match in-game message name for boss
    switch (bossName) {
      case "Hueycoatl":
        return HiscoreSkill.THE_HUEYCOATL.getName();
      case "Lunar Chest":
        return HiscoreSkill.LUNAR_CHESTS.getName();
      case "Chambers of Xeric Challenge Mode":
        return HiscoreSkill.CHAMBERS_OF_XERIC_CHALLENGE_MODE.getName();
      case "easy":
        return HiscoreSkill.CLUE_SCROLL_EASY.getName();
      case "medium":
        return HiscoreSkill.CLUE_SCROLL_MEDIUM.getName();
      case "hard":
        return HiscoreSkill.CLUE_SCROLL_HARD.getName();
      case "elite":
        return HiscoreSkill.CLUE_SCROLL_ELITE.getName();
      case "master":
        return HiscoreSkill.CLUE_SCROLL_MASTER.getName();
      default:
        return bossName;
    }
  }

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (boss == null) {
      log.warn("hiscore task with id {} has null boss!", getId());
      return false;
    }
    if (fixedBoss == null) {
      fixedBoss = KCTask.fixBossName(boss);
    }
    KillCount hiscore = playerInfo.getHiscores().get(fixedBoss);
    if (hiscore == null) return false;
    return hiscore.getCount() >= total;
  }
}
