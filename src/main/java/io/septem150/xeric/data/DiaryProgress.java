package io.septem150.xeric.data;

import java.util.List;
import lombok.Data;
import net.runelite.api.Client;

@Data
public class DiaryProgress {
  private KourendDiary diary;
  private int count;
  private boolean completed;

  public static final List<KourendDiary> trackedDiaries =
      List.of(KourendDiary.EASY, KourendDiary.MEDIUM, KourendDiary.HARD, KourendDiary.ELITE);

  public static DiaryProgress from(Client client, KourendDiary diary) {
    if (client == null || !client.isClientThread()) {
      throw new RuntimeException("must be on client thread");
    }
    DiaryProgress progress = new DiaryProgress();
    progress.diary = diary;
    progress.count = diary.getTaskCount(client);
    progress.completed = diary.getCompleted(client);
    return progress;
  }
}
