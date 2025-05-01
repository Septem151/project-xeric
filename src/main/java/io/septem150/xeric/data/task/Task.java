package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import lombok.Data;
import lombok.NonNull;
import net.runelite.client.game.SpriteManager;

@Data
public abstract class Task {
  private int id;
  private String name;
  private String type;
  private int tier;

  public abstract boolean checkCompletion(@NonNull PlayerInfo playerInfo);
  public abstract BufferedImage getIcon(SpriteManager spriteManager);
}
