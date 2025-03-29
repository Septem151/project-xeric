package io.septem150.xeric.data.player;

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
  /*
  TODO: get images from Sprite cache instead of our own resources
   */
  CARRY(3235, 0, 0),
  ARTISAN(3334, 0, 20),
  PRODIGY(3332, 0, 40),
  PYROMANCER(3331, 0, 60),
  FIRESTARTER(3328, 0, 80),
  LEGACY(3335, 0, 100),
  IGNITOR(3333, 0, 150),
  RED_TOPAZ(3129, 0, 200),
  SAPPHIRE(3130, 0, 250),
  EMERALD(3131, 0, 300),
  RUBY(3132, 0, 350),
  DIAMOND(3133, 0, 400),
  DRAGONSTONE(3134, 0, 450),
  ONYX(3135, 0, 500),
  SPELLCASTER(3256, 0, 600),
  SNIPER(3254, 0, 700),
  XERICIAN(3229, 0, 800),
  COMPETITOR(3211, 0, 900),
  MAXED(3247, 0, Integer.MAX_VALUE);

  /** All ranks sorted by points needed to obtain. */
  public static final List<ClanRank> allRanks =
      Arrays.stream(ClanRank.values())
          .sorted(Comparator.comparingInt(r -> r.pointsNeeded))
          .collect(Collectors.toList());

  private final int archive;
  private final int file;
  private final int pointsNeeded;

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
