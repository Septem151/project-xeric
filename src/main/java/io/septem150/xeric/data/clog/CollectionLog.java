package io.septem150.xeric.data.clog;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.septem150.xeric.ProjectXericConfig;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;

@Slf4j
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

  @NonNull private final Client client;
  @NonNull private final ConfigManager configManager;
  @NonNull private final Gson gson;

  @Getter @Setter private boolean interfaceOpened = false;
  @Getter @NonNull private Instant lastUpdated = Instant.EPOCH;
  private final Set<ClogItem> items = new HashSet<>();

  public CollectionLog(Client client, ConfigManager configManager, Gson gson) {
    this.client = client;
    this.configManager = configManager;
    this.gson = gson;
  }

  public boolean add(@Nullable ClogItem item) {
    if (item == null) return false;
    final boolean added = items.add(item);
    if (added) lastUpdated = Instant.now();
    return added;
  }

  public void reset() {
    items.clear();
    lastUpdated = Instant.EPOCH;
  }

  public @NonNull ImmutableSet<ClogItem> getItems() {
    return ImmutableSet.copyOf(items);
  }

  public void saveToRSProfile() {
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP,
        ProjectXericConfig.CLOG_DATA_KEY,
        gson.toJson(
            new RSProfileClog(
                lastUpdated, items.stream().map(ClogItem::getId).collect(Collectors.toSet()))));
  }

  public void loadFromRSProfile() {
    try {
      boolean updatedFromProfile =
          fromRSProfileJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.GROUP, ProjectXericConfig.CLOG_DATA_KEY));

      if (!updatedFromProfile) {
        saveToRSProfile();
      }
    } catch (JsonParseException err) {
      log.error("malformed clog data in profile");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.CLOG_DATA_KEY);
    }
  }

  private boolean fromRSProfileJson(@Nullable String json) throws JsonParseException {
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

  @RequiredArgsConstructor
  private static class RSProfileClog implements Serializable {
    private final Instant lastUpdated;
    private final Set<Integer> itemIds;
  }
}
