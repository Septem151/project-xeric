package io.septem150.xeric.data;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.util.ImageUtil;

@Slf4j
@RequiredArgsConstructor
@Getter
@ToString
public enum AccountType {
  DEFAULT(0, HiscoreEndpoint.NORMAL, 32),
  IRONMAN(1, HiscoreEndpoint.IRONMAN, 2),
  ULTIMATE(2, HiscoreEndpoint.ULTIMATE_IRONMAN, 3),
  HARDCORE(3, HiscoreEndpoint.HARDCORE_IRONMAN, 10),
  RANKED_GIM(4, HiscoreEndpoint.NORMAL, 41),
  HARDCORE_GIM(5, HiscoreEndpoint.NORMAL, 42),
  UNRANKED_GIM(6, HiscoreEndpoint.NORMAL, 43);

  private static final int MODICONS_ARCHIVE_ID = 423;
  private static final int ICON_SIZE = 14;

  private final int varbValue;
  private final HiscoreEndpoint hiscoreEndpoint;
  private final int spriteId;

  public static AccountType fromVarbValue(int varbValue) {
    for (AccountType accountType : AccountType.values()) {
      if (accountType.varbValue == varbValue) {
        return accountType;
      }
    }
    log.warn("no account type with varbValue of {}", varbValue);
    return DEFAULT;
  }

  public void getImageAsync(@NonNull SpriteManager spriteManager, Consumer<BufferedImage> user) {
    spriteManager.getSpriteAsync(
        MODICONS_ARCHIVE_ID,
        this.getSpriteId(),
        image -> user.accept(ImageUtil.resizeImage(image, ICON_SIZE, ICON_SIZE, true)));
  }
}
