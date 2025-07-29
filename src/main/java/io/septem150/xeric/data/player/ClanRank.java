package io.septem150.xeric.data.player;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.text.WordUtils;

@RequiredArgsConstructor
@Getter
public enum ClanRank {
  CARRY(0, 3235, 0),
  ARTISAN(25, 3334, 0),
  PRODIGY(50, 3332, 0),
  PYROMANCER(100, 3331, 0),
  FIRESTARTER(150, 3328, 0),
  LEGACY(200, 3335, 0),
  IGNITOR(250, 3333, 0),
  RED_TOPAZ(300, 3129, 0),
  SAPPHIRE(400, 3130, 0),
  EMERALD(500, 3131, 0),
  RUBY(600, 3132, 0),
  DIAMOND(700, 3133, 0),
  DRAGONSTONE(850, 3134, 0),
  ONYX(1000, 3135, 0),
  SPELLCASTER(1100, 3256, 0),
  SNIPER(1200, 3254, 0),
  XERICIAN(1300, 3229, 0),
  COMPETITOR(1400, 3211, 0),
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

  public BufferedImage getImage(@NonNull SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(archive, file)), 32, 32, true);
  }

  public void getImageAsync(@NonNull SpriteManager spriteManager, Consumer<BufferedImage> user) {
    spriteManager.getSpriteAsync(
        archive, file, image -> user.accept(ImageUtil.resizeImage(image, 32, 32, true)));
  }
}
