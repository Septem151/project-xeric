package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.runelite.client.game.SpriteManager;

@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Task implements Serializable {
  protected static final int ICON_SIZE = 20;

  @EqualsAndHashCode.Include private int id;
  private String icon;
  private String name;
  private TaskType type;
  private int tier;
  int slayerPoints;

  public abstract boolean checkCompletion(@NonNull PlayerInfo playerInfo);

  public abstract BufferedImage getIcon(SpriteManager spriteManager);
}
