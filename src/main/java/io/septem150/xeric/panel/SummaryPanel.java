package io.septem150.xeric.panel;

import java.awt.Color;
import java.awt.Font;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.FontManager;

public final class SummaryPanel extends PanelBase {
  @Inject
  private SummaryPanel() {
    super();
    // TODO: Find a better way to layout components other than BoxLayout
    BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(layout);
    JLabel header = new JLabel("<html>Xerician Rank");
    header.setFont(FontManager.getRunescapeFont().deriveFont(Font.BOLD, 20));
    header.setForeground(Color.GREEN.brighter());
    header.setHorizontalAlignment(SwingConstants.CENTER);
    header.setBorder(new EmptyBorder(0, 0, 5, 0));
    add(header);
  }

  @Override
  protected void reload() {}
}
