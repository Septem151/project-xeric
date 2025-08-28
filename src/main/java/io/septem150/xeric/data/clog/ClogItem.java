package io.septem150.xeric.data.clog;

import lombok.*;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ClogItem {
  private final int id;
  @NonNull private final String name;
}
