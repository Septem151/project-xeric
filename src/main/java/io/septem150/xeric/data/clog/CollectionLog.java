package io.septem150.xeric.data.clog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class CollectionLog {
  private Instant lastOpened;
  private List<ClogItem> items = new ArrayList<>();

  public void add(ClogItem clogItem) {
    items.add(clogItem);
  }

  public int size() {
    return items.size();
  }

  public Set<Integer> getItemIds() {
    return items.stream().map(ClogItem::getId).collect(Collectors.toSet());
  }

  public static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
  public static final int COLLECTION_LOG_TRANSMIT_SCRIPT_ID = 4100;

  public static final int CLOG_TOP_TABS_ENUM_ID = 2102;
  public static final int CLOG_SUB_TABS_PARAM_ID = 683;
  public static final int CLOG_SUB_TAB_ITEMS_PARAM_ID = 690;
  public static final int ITEM_REPLACEMENT_MAPPING_ENUM_ID = 3721;
  public static final int[] UNUSED_PROSPECTOR_ITEM_IDS = new int[] {29472, 29474, 29476, 29478};
}
