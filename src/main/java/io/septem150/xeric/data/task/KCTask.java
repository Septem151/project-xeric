package io.septem150.xeric.data.task;

import io.septem150.xeric.data.KillCount;
import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.util.ImageUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class KCTask extends Task {
  @AllArgsConstructor
  class Coord {
    int archive;
    int file;
  }

  private final static Map<String, Coord> iconMap = new HashMap<>();

  private String boss;
  private int total;

  public KCTask() {
    if (iconMap.isEmpty()) {
      iconMap.put(HiscoreSkill.CHAMBERS_OF_XERIC_CHALLENGE_MODE.getName(), new Coord(4296, 0));
      iconMap.put(HiscoreSkill.CHAMBERS_OF_XERIC.getName(), new Coord(4288, 0));
      iconMap.put(HiscoreSkill.ALCHEMICAL_HYDRA.getName(), new Coord(4289, 0));
      iconMap.put(HiscoreSkill.AMOXLIATL.getName(), new Coord(5639, 0));
      iconMap.put(HiscoreSkill.THE_HUEYCOATL.getName(), new Coord(5640, 0));
      iconMap.put(HiscoreSkill.SARACHNIS.getName(), new Coord(4269, 0));
      iconMap.put(HiscoreSkill.HESPORI.getName(), new Coord(4271, 0));
      iconMap.put(HiscoreSkill.SKOTIZO.getName(), new Coord(4272, 0));
      iconMap.put(HiscoreSkill.LUNAR_CHESTS.getName(), new Coord(5637, 0));
      iconMap.put(HiscoreSkill.SOL_HEREDIT.getName(), new Coord(5636, 0));
      iconMap.put(HiscoreSkill.COLOSSEUM_GLORY.getName(), new Coord(5862, 0));
      iconMap.put(HiscoreSkill.WINTERTODT.getName(), new Coord(4266, 0));
      iconMap.put(HiscoreSkill.MIMIC.getName(), new Coord(4260, 0));
      iconMap.put("Clues", new Coord(5853, 0));
    }
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
    Coord coord = iconMap.get(name);
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(coord.archive, coord.file)), 20, 20);
  }
}
