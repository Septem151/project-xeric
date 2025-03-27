package io.septem150.xeric.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Data;

@Data
public class PlayerData {
  private @Nullable String username;
  private Map<String, Integer> quests = new HashMap<>();
  private Map<String, Integer> diaries = new HashMap<>();
  private Map<String, Integer> levels = new HashMap<>();
  private List<Integer> caTasks = new ArrayList<>();
  private List<Integer> clogItems = new ArrayList<>();
  private Instant timestamp = Instant.now();

  public void clear() {
    username = null;
    quests.clear();
    diaries.clear();
    levels.clear();
    caTasks.clear();
    clogItems.clear();
    timestamp = Instant.now();
  }
}
