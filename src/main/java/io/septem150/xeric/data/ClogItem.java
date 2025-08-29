package io.septem150.xeric.data;

import lombok.*;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ClogItem {
  private final int id;
  @NonNull private final String name;
}
