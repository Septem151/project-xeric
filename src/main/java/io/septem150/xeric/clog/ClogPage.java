package io.septem150.xeric.clog;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class ClogPage {
  private final int tabStructId;
  private final int pageStructId;
  private final @NonNull String name;
  private final List<ClogItem> items = new ArrayList<>();

  public void addItem(ClogItem clogItem) {
    items.add(clogItem);
  }
}
