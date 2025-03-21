package io.septem150.xeric.clog;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.NonNull;

public interface ClogStore {
  List<ClogPage> getAllPages();

  Optional<ClogPage> getPageByStructId(int pageStructId);

  Optional<ClogPage> getPageByName(@NonNull String name);

  Optional<ClogPage> getPageByName(@NonNull Pattern pattern);
}
