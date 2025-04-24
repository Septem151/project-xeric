package io.septem150.xeric.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@RequiredArgsConstructor
@Getter
public enum KourendDiary {
  EASY("Easy", 7933, 7925),
  MEDIUM("Medium", 7934, 7926),
  HARD("Hard", 7935, 7927),
  ELITE("Elite", 7936, 7928);

  private final String name;
  private final int countVarb;
  private final int completedVarb;

  @Override
  public String toString() {
    return name;
  }

  public int getTaskCount(Client client) {
    if (client == null || !client.isClientThread()) {
      throw new RuntimeException("must be on client thread");
    }
    return client.getVarbitValue(this.countVarb);
  }

  public boolean getCompleted(Client client) {
    if (client == null || !client.isClientThread()) {
      throw new RuntimeException("must be on client thread");
    }
    return client.getVarbitValue(this.completedVarb) == 1;
  }
}
