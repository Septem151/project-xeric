package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import net.runelite.client.game.SpriteManager;

@Data
public abstract class Task {
  protected static final int ICON_SIZE = 20;

  private int id;
  @Nullable private String icon;
  private String name;
  private String type;
  private Integer tier;

  public abstract boolean checkCompletion(@NonNull PlayerInfo playerInfo);

  public abstract BufferedImage getIcon(SpriteManager spriteManager);
}
