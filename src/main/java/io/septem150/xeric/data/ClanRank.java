package io.septem150.xeric.data;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.text.WordUtils;

@RequiredArgsConstructor
@Getter
public enum ClanRank {
  CARRY(0, 3235, 0),
  ARTISAN(20, 3334, 0),
  PRODIGY(40, 3332, 0),
  PYROMANCER(60, 3331, 0),
  FIRESTARTER(80, 3328, 0),
  LEGACY(100, 3335, 0),
  IGNITOR(150, 3333, 0),
  RED_TOPAZ(200, 3129, 0),
  SAPPHIRE(250, 3130, 0),
  EMERALD(300, 3131, 0),
  RUBY(350, 3132, 0),
  DIAMOND(400, 3133, 0),
  DRAGONSTONE(450, 3134, 0),
  ONYX(500, 3135, 0),
  SPELLCASTER(600, 3256, 0),
  SNIPER(700, 3254, 0),
  XERICIAN(800, 3229, 0),
  COMPETITOR(900, 3211, 0),
  MAXED(Integer.MAX_VALUE, 3247, 0);

  /** All ranks sorted by points needed to obtain. */
  public static final List<ClanRank> allRanks =
      Arrays.stream(ClanRank.values())
          .sorted(Comparator.comparingInt(r -> r.pointsNeeded))
          .collect(Collectors.toList());

  private final int pointsNeeded;
  private final int archive;
  private final int file;

  public static ClanRank fromPoints(int points) {
    ClanRank obtainedRank = ClanRank.CARRY;
    for (ClanRank rank : allRanks) {
      if (points < rank.pointsNeeded) break;
      obtainedRank = rank;
    }
    return obtainedRank;
  }

  public ClanRank getNextRank() {
    return allRanks.get(Math.min(allRanks.indexOf(this) + 1, allRanks.size() - 1));
  }

  @Override
  public String toString() {
    return WordUtils.capitalizeFully(String.format("%s Rank", this.name()));
  }

  public BufferedImage getImage(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(archive, file)), 32, 32, true);
  }
}
