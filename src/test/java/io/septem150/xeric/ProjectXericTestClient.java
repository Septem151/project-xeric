package io.septem150.xeric;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ProjectXericTestClient {
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(ProjectXericPlugin.class);
    RuneLite.main(args);
  }
}
