package io.septem150.xeric.clog;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.runelite.api.ItemComposition;

@Getter
@ToString
public class ClogItem {
  private static final Map<Integer, Integer> ITEM_ID_REPLACEMENT_MAP =
      new ImmutableMap.Builder<Integer, Integer>()
          .put(10859, 25617) // Tea flask
          .put(10877, 25618) // Red satchel
          .put(10878, 25619) // Green satchel
          .put(10879, 25620) // Red satchel
          .put(10880, 25621) // Black satchel
          .put(10881, 25622) // Gold satchel
          .put(10882, 25623) // Rune satchel
          .put(13273, 25624) // Unsired
          .put(12019, 25627) // Coal bag
          .put(12020, 25628) // Gem bag
          .put(24882, 25629) // Plank sack
          .put(12854, 25630) // Flamtaer bag
          .build();

  private final int id;
  private @NonNull final String name;
  @Setter private transient int quantity;

  public ClogItem(int id, @NonNull String name, int quantity) {
    this.id = ITEM_ID_REPLACEMENT_MAP.getOrDefault(id, id);
    this.name = name;
    this.quantity = quantity;
  }

  public ClogItem(int id, @NonNull String name) {
    this(id, name, 0);
  }

  public static ClogItem fromItemComposition(ItemComposition itemComposition) {
    return new ClogItem(itemComposition.getId(), itemComposition.getMembersName(), 0);
  }

  public boolean isObtained() {
    return quantity > 0;
  }
}
