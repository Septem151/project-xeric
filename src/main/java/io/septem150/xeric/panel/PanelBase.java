package io.septem150.xeric.panel;

import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public abstract class PanelBase extends PluginPanel {
  protected boolean active;

  protected PanelBase() {
    super(false);
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setBackground(ColorScheme.DARK_GRAY_COLOR);
  }

  protected abstract void reload();

  @Override
  public void onActivate() {
    active = true;
    reload();
  }

  @Override
  public void onDeactivate() {
    active = false;
  }
}
