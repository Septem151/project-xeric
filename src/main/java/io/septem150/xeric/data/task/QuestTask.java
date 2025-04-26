package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.runelite.api.QuestState;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestTask extends Task {
  private String quest;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getQuests().stream()
        .anyMatch(
            questProgress ->
                questProgress.getQuest().getName().equals(quest)
                    && questProgress.getState() == QuestState.FINISHED);
  }
}
