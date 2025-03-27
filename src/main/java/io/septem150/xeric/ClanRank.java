package io.septem150.xeric;

import io.septem150.xeric.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.text.WordUtils;

@Getter
public enum ClanRank {
  /*
  TODO: get images from Sprite cache instead of our own resources
   */
  BAG(1, "ranks/bag.png", 0),
  ARTISAN(2, "ranks/artisan.png", 20),
  PRODIGY(3, "ranks/prodigy.png", 40),
  PYROMANCER(4, "ranks/pyromancer.png", 60),
  FIRESTARTER(5, "ranks/firestarter.png", 80),
  LEGACY(6, "ranks/legacy.png", 100),
  IGNITOR(7, "ranks/ignitor.png", 150),
  RED_TOPAZ(8, "ranks/red_topaz.png", 200),
  SAPPHIRE(9, "ranks/sapphire.png", 250),
  EMERALD(10, "ranks/emerald.png", 300),
  RUBY(11, "ranks/ruby.png", 350),
  DIAMOND(12, "ranks/diamond.png", 400),
  DRAGONSTONE(13, "ranks/dragonstone.png", 450),
  ONYX(14, "ranks/onyx.png", 500),
  SPELLCASTER(15, "ranks/spellcaster.png", 600),
  SNIPER(16, "ranks/sniper.png", 700),
  XERICIAN(17, "ranks/xerician.png", 800),
  COMPETITOR(18, "ranks/competitor.png", 900),
  MAXED(19, "ranks/maxed.png", Integer.MAX_VALUE);

  private final int id;
  private final BufferedImage image;
  private final int pointsNeeded;

  ClanRank(int id, String imageName, int pointsNeeded) {
    this.id = id;
    image = ResourceUtil.getImage(imageName, 32, 32);
    this.pointsNeeded = pointsNeeded;
  }

  public static Optional<ClanRank> fromId(int id) {
    return Arrays.stream(values()).filter(clanRank -> clanRank.id == id).findFirst();
  }

  public static Optional<ClanRank> fromName(String name) {
    return Arrays.stream(values())
        .filter(clanRank -> clanRank.name().equalsIgnoreCase(name))
        .findFirst();
  }

  public static ClanRank fromPoints(int points) {
    ClanRank obtainedRank = ClanRank.BAG;
    for (ClanRank rank : values()) {
      if (points < rank.pointsNeeded) break;
      obtainedRank = rank;
    }
    return obtainedRank;
  }

  public ClanRank getNextRank() {
    return ClanRank.fromId(this.getId() + 1).orElse(this);
  }

  @Override
  public String toString() {
    return WordUtils.capitalizeFully(String.format("%s Rank", this.name()));
  }
}
