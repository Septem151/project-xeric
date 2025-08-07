package io.septem150.xeric.data.player;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.util.ImageUtil;

@RequiredArgsConstructor
@Getter
public enum AccountType {
  INVALID("Invalid", -1, -1, HiscoreEndpoint.NORMAL),
  DEFAULT("Default", 0, 32, HiscoreEndpoint.NORMAL),
  IRONMAN("Ironman", 1, 2, HiscoreEndpoint.IRONMAN),
  ULTIMATE("Ultimate Ironman", 2, 3, HiscoreEndpoint.ULTIMATE_IRONMAN),
  HARDCORE("Hardcore Ironman", 3, 10, HiscoreEndpoint.HARDCORE_IRONMAN),
  RANKED_GIM("Ranked Group Ironman", 4, 41, HiscoreEndpoint.NORMAL),
  HARDCORE_GIM("Hardcore Group Ironman", 5, 42, HiscoreEndpoint.NORMAL),
  UNRANKED_GIM("Unranked Group Ironman", 6, 43, HiscoreEndpoint.NORMAL);

  private static final int MODICONS_ARCHIVE_ID = 423;

  private final String refName;
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

  public static AccountType fromName(String name) {
    for (AccountType accountType : AccountType.values()) {
      if (accountType.refName.equals(name)) {
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
    return ImageUtil.resizeImage(Objects.requireNonNull(sprite), 14, 14, true);
  }

  public void getImageAsync(@NonNull SpriteManager spriteManager, Consumer<BufferedImage> user) {
    spriteManager.getSpriteAsync(
        MODICONS_ARCHIVE_ID,
        this.getSpriteId(),
        image -> user.accept(ImageUtil.resizeImage(image, 14, 14, true)));
  }
}
