package io.septem150.xeric.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
@Getter
public class PlayerData {
  private @Nullable String username;
  private boolean storingClogData;
  private @NonNull Map<String, Integer> quests = new HashMap<>();
  private @NonNull Map<String, Integer> diaries = new HashMap<>();
  private @NonNull Map<String, Integer> levels = new HashMap<>();
  private @NonNull List<Integer> caTasks = new ArrayList<>();
  private @NonNull List<Integer> clogItems = new ArrayList<>();
  private @NonNull List<Integer> tasks = new ArrayList<>();
  private @NonNull Instant timestamp = Instant.now();

  public void clear() {
    username = null;
    storingClogData = false;
    quests.clear();
    diaries.clear();
    levels.clear();
    caTasks.clear();
    clogItems.clear();
    tasks.clear();
    timestamp = Instant.now();
  }
}
