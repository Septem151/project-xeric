package io.septem150.xeric.data.hiscore;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.NonNull;

public interface HiscoreStore {
  @NonNull List<Hiscore> getAll();

  Optional<Hiscore> getById(int id);

  Optional<Hiscore> getByUsername(@NonNull String username);

  Optional<Hiscore> getByUsername(@NonNull Pattern pattern);
}
