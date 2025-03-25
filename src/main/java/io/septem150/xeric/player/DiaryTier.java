package io.septem150.xeric.player;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DiaryTier {
  private final String tier;
  private List<Boolean> tasks = new ArrayList<>();
}
