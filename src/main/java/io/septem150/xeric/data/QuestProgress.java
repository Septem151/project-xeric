package io.septem150.xeric.data;

import java.util.List;
import lombok.*;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class QuestProgress {
  @NonNull private final Quest quest;
  @EqualsAndHashCode.Exclude @NonNull private QuestState state;

  public static final List<Quest> TRACKED_QUESTS =
      List.of(
          Quest.DRUIDIC_RITUAL,
          Quest.EAGLES_PEAK,
          Quest.RUNE_MYSTERIES,
          Quest.A_KINGDOM_DIVIDED,
          Quest.GETTING_AHEAD,
          Quest.THE_GARDEN_OF_DEATH,
          Quest.CHILDREN_OF_THE_SUN,
          Quest.TWILIGHTS_PROMISE,
          Quest.THE_HEART_OF_DARKNESS,
          Quest.X_MARKS_THE_SPOT,
          Quest.CLIENT_OF_KOUREND,
          Quest.THE_QUEEN_OF_THIEVES,
          Quest.THE_DEPTHS_OF_DESPAIR,
          Quest.THE_ASCENT_OF_ARCEUUS,
          Quest.THE_FORSAKEN_TOWER,
          Quest.TALE_OF_THE_RIGHTEOUS,
          Quest.PERILOUS_MOONS,
          Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE,
          Quest.AT_FIRST_LIGHT,
          Quest.DEATH_ON_THE_ISLE,
          Quest.MEAT_AND_GREET,
          Quest.ETHICALLY_ACQUIRED_ANTIQUITIES,
          Quest.THE_FINAL_DAWN,
          Quest.SHADOWS_OF_CUSTODIA,
          Quest.SCRAMBLED);
}
