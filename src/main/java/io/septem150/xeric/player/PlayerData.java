package io.septem150.xeric.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class PlayerData {
  private final String username;
  private Map<String, Integer> quests = new HashMap<>();
  private Map<String, DiaryTier> diaries = new HashMap<>();
  private Map<String, Integer> levels = new HashMap<>();
  private Map<String, Boolean> music = new HashMap<>();
  private List<Integer> caTasks = new ArrayList<>();
  private List<Integer> clogItems = new ArrayList<>();
  private Instant timestamp = Instant.now();
}
