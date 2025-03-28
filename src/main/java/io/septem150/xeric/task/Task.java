package io.septem150.xeric.task;

import io.septem150.xeric.data.PlayerData;
import lombok.Data;

@Data
public abstract class Task {
  private int id;
  private String icon;
  private String name;
  private String type;
  private int tier;

  public abstract boolean checkCompletion(PlayerData playerData);
}
