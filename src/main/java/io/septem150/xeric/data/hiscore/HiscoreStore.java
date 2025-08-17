package io.septem150.xeric.data.hiscore;

import java.util.List;
import lombok.NonNull;

public interface HiscoreStore {
  @NonNull List<Hiscore> getAll();
}
