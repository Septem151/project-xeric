package io.septem150.xeric_v2;

import io.septem150.xeric.data.player.KillCount;
import io.septem150.xeric.data.task.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Data;

@Data
public class PlayerData {
  private String username;
  private Integer accountType;
  private boolean slayerException;

  @Nullable
  private PlayerClanData clan;

  private Map<Integer, Integer> quests = new HashMap<>();
  private List<AchievementDiaryTierData> achievementDiaryTiers = new ArrayList<>();
  private Map<String, Integer> skills = new HashMap<>();
  private Map<String, KillCount> killCounts = new HashMap<>();
  private Map<Integer, Integer> combatAchievementTiers = new HashMap<>();
  private Map<Integer, Integer> items = new HashMap<>();
  private List<Task> tasks = new ArrayList<>();

}
