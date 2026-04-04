package io.septem150.xeric.util;

import io.septem150.xeric.ProjectXericPlugin;
import io.septem150.xeric.data.ProjectXericApiClient;
import io.septem150.xeric.data.task.KCTask;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
public class ImageService {
  private static final int ICON_SIZE = 20;
  private static final File ICONS_DIR = new File(RuneLite.RUNELITE_DIR, "project-xeric/icons");

  private final ProjectXericApiClient apiClient;
  private final Map<TaskType, BufferedImage> defaultIcons = new EnumMap<>(TaskType.class);

  @Inject
  public ImageService(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
    ICONS_DIR.mkdirs();
    for (TaskType type : TaskType.values()) {
      String filename = "default_" + type.getName() + "_task.png";
      defaultIcons.put(
          type,
          ImageUtil.resizeImage(
              ImageUtil.loadImageResource(ProjectXericPlugin.class, "images/" + filename),
              ICON_SIZE,
              ICON_SIZE,
              true));
    }
  }

  public BufferedImage getDefaultIcon(TaskType type) {
    return defaultIcons.get(type);
  }

  public void loadTaskIcon(Task task, Consumer<BufferedImage> callback) {
    String icon = task.getIcon();
    if (icon == null && task instanceof KCTask) {
      icon = deriveKcIconFilename((KCTask) task);
    }
    if (icon == null) {
      return;
    }
    final String iconFilename = icon;

    // check disk cache
    File cachedFile = new File(ICONS_DIR, iconFilename);
    if (cachedFile.exists()) {
      try {
        BufferedImage image = ImageIO.read(cachedFile);
        if (image != null) {
          callback.accept(ImageUtil.resizeImage(image, ICON_SIZE, ICON_SIZE, true));
          return;
        }
      } catch (IOException e) {
        log.warn("Corrupt cached icon, deleting: {}", iconFilename);
        cachedFile.delete();
      }
    }

    // fetch from server
    apiClient
        .fetchTaskIcon(iconFilename)
        .thenAccept(
            bytes -> {
              try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                if (image == null) return;
                try {
                  ICONS_DIR.mkdirs();
                  ImageIO.write(image, "png", cachedFile);
                } catch (IOException e) {
                  log.warn("Failed to cache icon: {}", iconFilename, e);
                }
                callback.accept(ImageUtil.resizeImage(image, ICON_SIZE, ICON_SIZE, true));
              } catch (IOException e) {
                log.warn("Failed to read task icon: {}", iconFilename, e);
              }
            })
        .exceptionally(
            err -> {
              log.warn("Failed to fetch task icon: {}", iconFilename, err);
              return null;
            });
  }

  private static String deriveKcIconFilename(KCTask task) {
    String boss = task.getBoss();
    if (boss == null) return null;
    return boss.toLowerCase().replace(" ", "_").replaceAll("[():]", "") + ".png";
  }

  public void clearCache() {
    File[] files = ICONS_DIR.listFiles();
    if (files != null) {
      for (File file : files) {
        file.delete();
      }
    }
  }
}
