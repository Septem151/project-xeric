package io.septem150.xeric.data.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class ClogData {
  private @NonNull List<Integer> items = new ArrayList<>();
  private @NonNull Instant lastUpdated = Instant.now();

  public ClogData(Collection<Integer> items) {
    this.items.addAll(items);
  }
}
