package io.septem150.xeric.data;

import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.util.ImageUtil;

@RequiredArgsConstructor
@Getter
public enum AccountType {
  INVALID(-1, -1, HiscoreEndpoint.NORMAL),
  DEFAULT(0, 32, HiscoreEndpoint.NORMAL),
  IRONMAN(1, 2, HiscoreEndpoint.IRONMAN),
  ULTIMATE(2, 3, HiscoreEndpoint.ULTIMATE_IRONMAN),
  HARDCORE(3, 10, HiscoreEndpoint.HARDCORE_IRONMAN),
  RANKED_GIM(4, 41, HiscoreEndpoint.NORMAL),
  HARDCORE_GIM(5, 42, HiscoreEndpoint.NORMAL),
  UNRANKED_GIM(6, 43, HiscoreEndpoint.NORMAL);

  private static final int MODICONS_ARCHIVE_ID = 423;

  private final int varbValue;
  private final int spriteId;
  private final HiscoreEndpoint hiscoreEndpoint;

  public static AccountType fromVarbValue(int varbValue) {
    for (AccountType accountType : AccountType.values()) {
      if (accountType.varbValue == varbValue) {
        return accountType;
      }
    }
    return AccountType.INVALID;
  }

  public BufferedImage getImage(@NonNull SpriteManager spriteManager) {
    if (this == AccountType.INVALID) {
      return DEFAULT.getImage(spriteManager);
    }
    final BufferedImage sprite = spriteManager.getSprite(MODICONS_ARCHIVE_ID, this.getSpriteId());
    return ImageUtil.resizeImage(Objects.requireNonNull(sprite), 14, 14, false);
  }
}
