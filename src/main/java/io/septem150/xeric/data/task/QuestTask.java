package io.septem150.xeric.data.task;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.data.player.QuestProgress;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class QuestTask extends Task {
  @SerializedName("detail")
  private String quest;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (quest == null) {
      log.warn("quest task with id {} has null quest!", getId());
      return false;
    }
    Quest questObj =
        QuestProgress.TRACKED_QUESTS.stream()
            .filter(q -> q.getName().equals(quest))
            .findFirst()
            .orElse(null);
    if (questObj == null) {
      log.warn("unknown quest: {}", quest);
      return false;
    }
    QuestProgress questProgress = playerInfo.getQuests().get(questObj);
    if (questProgress == null) return false;
    return questProgress.getState() == QuestState.FINISHED;
  }
}
