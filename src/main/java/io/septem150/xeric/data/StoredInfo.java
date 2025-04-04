package io.septem150.xeric.data;

import io.septem150.xeric.data.task.Task;
import java.util.List;
import lombok.Data;

@Data
public class StoredInfo {
  private CollectionLog collectionLog;
  private List<Task> tasks;
  private boolean slayerException;
}
