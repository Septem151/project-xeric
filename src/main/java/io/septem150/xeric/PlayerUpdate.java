package io.septem150.xeric;

import io.septem150.xeric.data.PlayerInfo;
import java.util.function.Consumer;
import lombok.Data;

@Data
public class PlayerUpdate {
  private final Consumer<PlayerInfo> action;

  public PlayerUpdate(Consumer<PlayerInfo> action) {
    this.action = action;
  }
}
