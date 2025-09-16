package io.septem150.xeric.data.player;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
  BRONZE(25, 3153, 0),
  IRON(50, 3154, 0),
  STEEL(100, 3155, 0),
  OPAL(150, 3127, 0),
  JADE(200, 3128, 0),
  RED_TOPAZ(250, 3129, 0),
  SAPPHIRE(300, 3130, 0),
  EMERALD(400, 3131, 0),
  RUBY(500, 3132, 0),
  DIAMOND(600, 3133, 0),
  DRAGONSTONE(700, 3134, 0),
  ONYX(850, 3135, 0),
  ZENYTE(1000, 3136, 0),
  SPELLCASTER(1150, 3256, 0),
  SNIPER(1300, 3254, 0),
  XERICIAN(1450, 3229, 0),
  MAXED(1650, 3247, 0);

  /** All ranks sorted by points needed to obtain. */
  public static final List<ClanRank> ALL_RANKS =
      Arrays.stream(ClanRank.values())
          .sorted(Comparator.comparingInt(r -> r.pointsNeeded))
          .collect(Collectors.toList());

  private final int pointsNeeded;
  private final int archive;
  private final int file;

  public static ClanRank fromPoints(int points) {
    ClanRank obtainedRank = ClanRank.CARRY;
    for (ClanRank rank : ALL_RANKS) {
      if (points < rank.pointsNeeded) break;
      obtainedRank = rank;
    }
    return obtainedRank;
  }

  public ClanRank getNextRank() {
    return ALL_RANKS.get(Math.min(ALL_RANKS.indexOf(this) + 1, ALL_RANKS.size() - 1));
  }

  @Override
  public String toString() {
    return WordUtils.capitalizeFully(String.format("%s Rank", this.name()));
  }

  public void getImageAsync(@NonNull SpriteManager spriteManager, Consumer<BufferedImage> user) {
    spriteManager.getSpriteAsync(
        archive, file, image -> user.accept(ImageUtil.resizeImage(image, 32, 32, true)));
  }
}
