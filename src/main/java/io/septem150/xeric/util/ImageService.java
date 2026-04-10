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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
  private static final Path ICONS_CACHE_DIR = ProjectXericConfig.CACHE_DIR.resolve("icons");
  private static final Path RANKS_CACHE_DIR = ProjectXericConfig.CACHE_DIR.resolve("ranks");

  private final ProjectXericApiClient apiClient;
  private final Map<TaskType, BufferedImage> defaultIcons = new EnumMap<>(TaskType.class);

  @Inject
  public ImageService(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
    try {
      Files.createDirectories(ICONS_CACHE_DIR);
      Files.createDirectories(RANKS_CACHE_DIR);
    } catch (IOException e) {
      log.warn("Failed to create icon cache directories", e);
    }
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
        ICONS_CACHE_DIR,
        iconFilename,
        apiClient.fetchTaskIcon(iconFilename),
        TASK_ICON_SIZE,
        callback);
  }

  public void loadRankIcon(ClanRank rank, Consumer<BufferedImage> callback) {
    if (rank == null || rank.getIcon() == null) return;
    String iconFilename = rank.getIcon();
    loadIcon(
        RANKS_CACHE_DIR,
        iconFilename,
        apiClient.fetchRankIcon(iconFilename),
        RANK_ICON_SIZE,
        callback);
  }

  private void loadIcon(
      Path cacheDir,
      String filename,
      CompletableFuture<byte[]> fetchFuture,
      int size,
      Consumer<BufferedImage> callback) {
    Path resolvedPath = cacheDir.resolve(filename).normalize();
    // after resolving path, compare against cacheDir to prevent relative escaping via "../"
    if (!resolvedPath.startsWith(cacheDir)) {
      log.warn("Invalid icon cache directory: {}", filename);
      return;
    }

    if (Files.exists(resolvedPath)) {
      try (InputStream in = Files.newInputStream(resolvedPath)) {
        BufferedImage image = ImageIO.read(in);
        if (image != null) {
          callback.accept(resize(image, size));
          return;
        }
      } catch (IOException e) {
        log.warn("Corrupt cached icon, deleting: {}", filename);
        try {
          Files.deleteIfExists(resolvedPath);
        } catch (IOException ex) {
          log.warn("Failed to delete corrupt cached icon: {}", filename, ex);
        }
      }
    }

    fetchFuture
        .thenAccept(
            bytes -> {
              try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                if (image == null) return;
                callback.accept(resize(image, size));
                // Ensure cache dir exists before trying to write
                Files.createDirectories(cacheDir);
                ImageIO.write(image, "png", Files.newOutputStream(resolvedPath));
              } catch (IOException e) {
                log.warn("Failed to load or cache icon: {}", filename, e);
              }
            })
        .exceptionally(
            err -> {
              log.warn("Failed to fetch icon: {}", filename, err);
              return null;
            });
  }
}
