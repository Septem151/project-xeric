package io.septem150.xeric.data.player;

import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@RequiredArgsConstructor
@Getter
public enum AccountType {
  INVALID(-1, -1),
  DEFAULT(0, 32),
  IRONMAN(1, 2),
  ULTIMATE(2, 3),
  HARDCORE(3, 10),
  RANKED_GIM(4, 41),
  HARDCORE_GIM(5, 42),
  UNRANKED_GIM(6, 43);

  private static final int MODICONS_ARCHIVE_ID = 423;

  private final int varbValue;
  private final int spriteId;

  public static AccountType fromVarbValue(int varbValue) {
    for (AccountType accountType : AccountType.values()) {
      if (accountType.varbValue == varbValue) {
        return accountType;
      }
    }
    return AccountType.INVALID;
  }

  public BufferedImage getImage(@NonNull Client client, @NonNull SpriteManager spriteManager) {
    assert client.isClientThread();
    if (this == AccountType.INVALID) {
      return DEFAULT.getImage(client, spriteManager);
    }
    final BufferedImage sprite = spriteManager.getSprite(MODICONS_ARCHIVE_ID, this.getSpriteId());
    return ImageUtil.resizeImage(Objects.requireNonNull(sprite), 14, 14, false);
  }
}
