package io.septem150.xeric.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CollectionLog {
  private Instant lastOpened;
  private List<ClogItem> items = new ArrayList<>();

  public static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
  public static final int COLLECTION_LOG_TRANSMIT_SCRIPT_ID = 4100;
}
