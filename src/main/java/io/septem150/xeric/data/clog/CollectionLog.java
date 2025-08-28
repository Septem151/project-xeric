package io.septem150.xeric.data.clog;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.septem150.xeric.ProjectXericConfig;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;

@Slf4j
@NoArgsConstructor
public class CollectionLog {
  // Clog Constants
  public static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
  public static final int COLLECTION_LOG_TRANSMIT_SCRIPT_ID = 4100;

  public static final int CLOG_TOP_TABS_ENUM_ID = 2102;
  public static final int CLOG_SUB_TABS_PARAM_ID = 683;
  public static final int CLOG_SUB_TAB_ITEMS_PARAM_ID = 690;
  public static final int ITEM_REPLACEMENT_MAPPING_ENUM_ID = 3721;
  public static final List<Integer> UNUSED_PROSPECTOR_ITEM_IDS =
      List.of(29472, 29474, 29476, 29478);

  @Getter @Setter private boolean interfaceOpened = false;
  @Getter @NonNull private Instant lastUpdated = Instant.EPOCH;
  @NonNull private final Set<ClogItem> items = new HashSet<>();

  @CanIgnoreReturnValue
  public boolean add(@Nullable ClogItem item) {
    if (item == null) return false;
    final boolean added = items.add(item);
    if (added) lastUpdated = Instant.now();
    return added;
  }

  public int size() {
    return items.size();
  }

  public void reset() {
    items.clear();
    lastUpdated = Instant.EPOCH;
  }

  public @NonNull ImmutableSet<ClogItem> getItems() {
    return ImmutableSet.copyOf(items);
  }

  public @NonNull String toRSProfileJson(@NonNull Gson gson) {
    return gson.toJson(
        new RSProfileClog(
            lastUpdated, items.stream().map(ClogItem::getId).collect(Collectors.toSet())));
  }

  @CanIgnoreReturnValue
  public boolean fromRSProfileJson(
      @NonNull Client client, @NonNull Gson gson, @Nullable String json) throws JsonParseException {
    if (json == null) return false;
    RSProfileClog profileClog = gson.fromJson(json, RSProfileClog.class);
    if (lastUpdated.isBefore(profileClog.lastUpdated)) {
      lastUpdated = profileClog.lastUpdated;
      items.clear();
      for (int itemId : profileClog.itemIds) {
        ClogItem clogItem = new ClogItem(itemId, client.getItemDefinition(itemId).getMembersName());
        items.add(clogItem);
      }
      return true;
    }
    return false;
  }

  public void saveToRSProfile(@NonNull ConfigManager configManager, @NonNull Gson gson) {
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.CONFIG_GROUP,
        ProjectXericConfig.CONFIG_KEY_CLOG,
        this.toRSProfileJson(gson));
  }

  public void loadFromRSProfile(
      @NonNull Client client, @NonNull ConfigManager configManager, @NonNull Gson gson) {
    try {
      boolean updatedFromProfile =
          this.fromRSProfileJson(
              client,
              gson,
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_CLOG));

      if (!updatedFromProfile) {
        this.saveToRSProfile(configManager, gson);
      }
    } catch (JsonParseException err) {
      log.error("malformed clog data in profile");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.CONFIG_GROUP, ProjectXericConfig.CONFIG_KEY_CLOG);
    }
  }

  @RequiredArgsConstructor
  private static class RSProfileClog {
    final Instant lastUpdated;
    final Set<Integer> itemIds;
  }
}
