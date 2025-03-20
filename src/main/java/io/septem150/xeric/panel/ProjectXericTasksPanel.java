package io.septem150.xeric.panel;

import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;

public class ProjectXericTasksPanel extends JPanel {

  @Inject
  private ProjectXericTasksPanel() {
    setLayout(new BorderLayout());
    setBackground(ColorScheme.DARK_GRAY_COLOR);

    JPanel container = new JPanel();
  }
}
