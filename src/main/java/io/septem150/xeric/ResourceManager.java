package io.septem150.xeric;

import java.awt.image.BufferedImage;
import lombok.NonNull;
import net.runelite.client.util.ImageUtil;

public class ResourceManager {
  public static BufferedImage getImage(@NonNull String name) {
    String imagePath = name;
    if (!name.startsWith("/")) {
      imagePath = String.format("images/%s", imagePath);
    }
    return ImageUtil.loadImageResource(ProjectXericPlugin.class, imagePath);
  }

  public static BufferedImage getImage(@NonNull String name, int width, int height) {
    return getImage(name, width, height, false);
  }

  public static BufferedImage getImage(
      @NonNull String name, int width, int height, boolean preserveAspectRatio) {
    BufferedImage image = getImage(name);
    return ImageUtil.resizeImage(image, width, height, preserveAspectRatio);
  }
}
