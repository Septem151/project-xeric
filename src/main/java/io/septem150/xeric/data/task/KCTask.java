package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.KillCount;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.util.ImageCoord;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.util.ImageUtil;

@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class KCTask extends Task {
  private static final Map<String, ImageCoord> iconMap = new HashMap<>();

  private String boss;
  private int total;

  static {
    iconMap.put(HiscoreSkill.CHAMBERS_OF_XERIC_CHALLENGE_MODE.getName(), new ImageCoord(4296, 0));
    iconMap.put(HiscoreSkill.CHAMBERS_OF_XERIC.getName(), new ImageCoord(4288, 0));
    iconMap.put(HiscoreSkill.ALCHEMICAL_HYDRA.getName(), new ImageCoord(4289, 0));
    iconMap.put(HiscoreSkill.AMOXLIATL.getName(), new ImageCoord(5639, 0));
    iconMap.put(HiscoreSkill.THE_HUEYCOATL.getName(), new ImageCoord(5640, 0));
    iconMap.put(HiscoreSkill.SARACHNIS.getName(), new ImageCoord(4269, 0));
    iconMap.put(HiscoreSkill.HESPORI.getName(), new ImageCoord(4271, 0));
    iconMap.put(HiscoreSkill.SKOTIZO.getName(), new ImageCoord(4272, 0));
    iconMap.put(HiscoreSkill.LUNAR_CHESTS.getName(), new ImageCoord(5637, 0));
    iconMap.put(HiscoreSkill.SOL_HEREDIT.getName(), new ImageCoord(5636, 0));
    iconMap.put(HiscoreSkill.COLOSSEUM_GLORY.getName(), new ImageCoord(5862, 0));
    iconMap.put(HiscoreSkill.WINTERTODT.getName(), new ImageCoord(4266, 0));
    iconMap.put(HiscoreSkill.MIMIC.getName(), new ImageCoord(4260, 0));
    iconMap.put(HiscoreSkill.YAMA.getName(), new ImageCoord(6346, 0));
    iconMap.put(HiscoreSkill.DOOM_OF_MOKHAIOTL.getName(), new ImageCoord(6347, 0));
    iconMap.put("Clues", new ImageCoord(5853, 0));
  }

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

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    String name = boss;
    if ("Lunar Chest".equals(name)) {
      name += "s";
    } else if ("Hueycoatl".equals(name)) {
      name = "The " + name;
    } else if (boss.contains("Clue")) {
      name = "Clues";
    }
    ImageCoord coord = iconMap.get(name);
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(coord.archive, coord.file)), 20, 20, true);
  }
}
