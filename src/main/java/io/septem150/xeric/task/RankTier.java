package io.septem150.xeric.task;

import java.util.List;
import lombok.Data;

@Data
public class RankTier {
  private List<Task> tasks;
  private int tier;
}
