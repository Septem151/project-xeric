package io.septem150.xeric.task;

import io.septem150.xeric.data.Hiscore;
import io.septem150.xeric.data.player.PlayerData;
import io.septem150.xeric.util.ImageCoord;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class HiscoreTask extends Task {
  private String boss;
  private int total;
  private transient String fixedBoss;
  private static final Map<String, ImageCoord> ICON_MAP = new HashMap<>();

  static {
    ICON_MAP.put(HiscoreSkill.CHAMBERS_OF_XERIC_CHALLENGE_MODE.getName(), new ImageCoord(4296, 0));
    ICON_MAP.put(HiscoreSkill.CHAMBERS_OF_XERIC.getName(), new ImageCoord(4288, 0));
    ICON_MAP.put(HiscoreSkill.ALCHEMICAL_HYDRA.getName(), new ImageCoord(4289, 0));
    ICON_MAP.put(HiscoreSkill.AMOXLIATL.getName(), new ImageCoord(5639, 0));
    ICON_MAP.put(HiscoreSkill.THE_HUEYCOATL.getName(), new ImageCoord(5640, 0));
    ICON_MAP.put(HiscoreSkill.SARACHNIS.getName(), new ImageCoord(4269, 0));
    ICON_MAP.put(HiscoreSkill.HESPORI.getName(), new ImageCoord(4271, 0));
    ICON_MAP.put(HiscoreSkill.SKOTIZO.getName(), new ImageCoord(4272, 0));
    ICON_MAP.put(HiscoreSkill.LUNAR_CHESTS.getName(), new ImageCoord(5637, 0));
    ICON_MAP.put(HiscoreSkill.SOL_HEREDIT.getName(), new ImageCoord(5636, 0));
    ICON_MAP.put(HiscoreSkill.COLOSSEUM_GLORY.getName(), new ImageCoord(5862, 0));
    ICON_MAP.put(HiscoreSkill.WINTERTODT.getName(), new ImageCoord(4266, 0));
    ICON_MAP.put(HiscoreSkill.MIMIC.getName(), new ImageCoord(4260, 0));
    ICON_MAP.put(HiscoreSkill.YAMA.getName(), new ImageCoord(6346, 0));
    ICON_MAP.put(HiscoreSkill.DOOM_OF_MOKHAIOTL.getName(), new ImageCoord(6347, 0));
    ICON_MAP.put("Clues", new ImageCoord(5853, 0));
  }

  @Override
  public boolean isCompleted(PlayerData playerData) {
    if (boss == null) {
      log.warn("hiscore task with id {} has null boss!", getId());
      return false;
    }
    if (fixedBoss == null) {
      fixedBoss = HiscoreTask.fixBossName(boss);
    }
    Hiscore hiscore = playerData.getHiscores().get(fixedBoss);
    if (hiscore == null) return false;
    return hiscore.getCount() >= total;
  }

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
  public BufferedImage getIcon(SpriteManager spriteManager) {
    if (fixedBoss == null) {
      fixedBoss = HiscoreTask.fixBossName(boss);
    }
    ImageCoord coord = ICON_MAP.get(fixedBoss.contains("Clue") ? "Clues" : fixedBoss);
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(coord.archive, coord.file)),
        ICON_SIZE,
        ICON_SIZE,
        true);
  }
}
