package io.septem150.xeric.data.task;

import io.septem150.xeric.data.PlayerInfo;
import lombok.Data;
import lombok.NonNull;

@Data
public abstract class Task {
  private int id;
  private String icon;
  private String name;
  private String type;
  private int tier;

  public abstract boolean checkCompletion(@NonNull PlayerInfo playerInfo);
}
