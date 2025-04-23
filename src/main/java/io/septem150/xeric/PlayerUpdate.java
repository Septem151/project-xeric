package io.septem150.xeric;

import io.septem150.xeric.data.PlayerInfo;
import java.util.function.Consumer;
import lombok.Data;

@Data
public class PlayerUpdate {
  private final Object source;
  private final Consumer<PlayerInfo> action;

  public PlayerUpdate(Object source, Consumer<PlayerInfo> action) {
    this.source = source;
    this.action = action;
  }
}
