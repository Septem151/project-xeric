package io.septem150.xeric.data;

import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class StoredInfo {
  private List<Integer> clogItems;
  private List<Integer> tasks;
  private boolean slayerException;
  private Instant lastUpdated;
}
