package io.septem150.xeric;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Project Xeric plugin tests.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
public class ProjectXericPluginTest {
  /**
   * Main entrypoint for running a test client with the plugin added.
   *
   * @param args String[] program args
   * @throws Exception if a failure occurs
   */
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(ProjectXericPlugin.class);
    RuneLite.main(args);
  }
}
