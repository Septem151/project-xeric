package io.septem150.xeric.util;

import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.ProjectXericPlugin;
import io.septem150.xeric.data.ProjectXericApiClient;
import io.septem150.xeric.data.player.ClanRank;
import io.septem150.xeric.data.task.KCTask;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
public class ImageService {
  private static final int TASK_ICON_SIZE = 20;
  private static final int RANK_ICON_SIZE = 32;
  private static final File ICONS_DIR = new File(ProjectXericConfig.CACHE_DIR, "icons");
  private static final File RANKS_DIR = new File(ProjectXericConfig.CACHE_DIR, "ranks");

  private final ProjectXericApiClient apiClient;
  private final Map<TaskType, BufferedImage> defaultIcons = new EnumMap<>(TaskType.class);

  @Inject
  public ImageService(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
    ICONS_DIR.mkdirs();
    RANKS_DIR.mkdirs();
    for (TaskType type : TaskType.values()) {
      defaultIcons.put(
          type,
          resize(
              ImageUtil.loadImageResource(
                  ProjectXericPlugin.class, "images/default_" + type.getName() + "_task.png"),
              TASK_ICON_SIZE));
    }
  }

  private static BufferedImage resize(BufferedImage image, int size) {
    return ImageUtil.resizeImage(image, size, size, true);
  }

  private static String resolveIconFilename(Task task) {
    if (task.getIcon() != null) return task.getIcon();
    if (task instanceof KCTask) {
      String boss = ((KCTask) task).getBoss();
      if (boss != null) {
        return boss.toLowerCase().replace(" ", "_").replaceAll("[():]", "") + ".png";
      }
    }
    return null;
  }

  public BufferedImage getDefaultIcon(TaskType type) {
    return defaultIcons.get(type);
  }

  public void loadTaskIcon(Task task, Consumer<BufferedImage> callback) {
    String iconFilename = resolveIconFilename(task);
    if (iconFilename == null) return;
    loadIcon(
        ICONS_DIR, iconFilename, apiClient.fetchTaskIcon(iconFilename), TASK_ICON_SIZE, callback);
  }

  public void loadRankIcon(ClanRank rank, Consumer<BufferedImage> callback) {
    if (rank == null || rank.getIcon() == null) return;
    String iconFilename = rank.getIcon();
    loadIcon(
        RANKS_DIR, iconFilename, apiClient.fetchRankIcon(iconFilename), RANK_ICON_SIZE, callback);
  }

  private void loadIcon(
      File cacheDir,
      String filename,
      CompletableFuture<byte[]> fetchFuture,
      int size,
      Consumer<BufferedImage> callback) {
    File cachedFile = new File(cacheDir, filename);

    if (cachedFile.exists()) {
      try {
        BufferedImage image = ImageIO.read(cachedFile);
        if (image != null) {
          callback.accept(resize(image, size));
          return;
        }
      } catch (IOException e) {
        log.warn("Corrupt cached icon, deleting: {}", filename);
        cachedFile.delete();
      }
    }

    fetchFuture
        .thenAccept(
            bytes -> {
              try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                if (image == null) return;
                try {
                  cacheDir.mkdirs();
                  ImageIO.write(image, "png", cachedFile);
                } catch (IOException e) {
                  log.warn("Failed to cache icon: {}", filename, e);
                }
                callback.accept(resize(image, size));
              } catch (IOException e) {
                log.warn("Failed to read icon: {}", filename, e);
              }
            })
        .exceptionally(
            err -> {
              log.warn("Failed to fetch icon: {}", filename, err);
              return null;
            });
  }
}
