package io.septem150.xeric.panel;

import io.septem150.xeric.ProjectXericManager;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
@Singleton
public class SummaryPanelV2 extends JPanel {
  public static final String TOOLTIP = "Player Summary";
  public static final String TAB_ICON = "summary_tab_icon.png";
  private static final String LOGGED_OUT_CONSTRAINT = "LOGGED_OUT";
  private static final String LOGGED_IN_CONSTRAINT = "LOGGED_IN";

  private final CardLayout layout = new CardLayout();
  private final JLabel clogNotice = new JLabel();

  private final ProjectXericManager manager;
  private final TaskListPanel taskListPanel;

  @Inject
  private SummaryPanelV2(
      ProjectXericManager manager, TaskListPanel taskListPanel, IdCardPanel idCardPanel) {
    this.manager = manager;
    this.taskListPanel = taskListPanel;
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    setLayout(layout);

    final JPanel loggedOutPanel = createLoggedOutPanel();
    add(loggedOutPanel, LOGGED_OUT_CONSTRAINT);

    final JPanel loggedInPanel = new JPanel();
    loggedInPanel.setLayout(new BoxLayout(loggedInPanel, BoxLayout.Y_AXIS));

    loggedInPanel.add(idCardPanel);
    loggedInPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    clogNotice.setText(
        "<html><body style='text-align:center'>Open the Collection Log to sync"
            + " data.</body></html>");
    clogNotice.setHorizontalAlignment(SwingConstants.CENTER);
    clogNotice.setAlignmentX(Component.CENTER_ALIGNMENT);
    clogNotice.setFont(FontManager.getRunescapeSmallFont());
    clogNotice.setForeground(ColorScheme.BRAND_ORANGE);
    clogNotice.setBorder(new EmptyBorder(0, 0, 5, 0));
    loggedInPanel.add(clogNotice);
    loggedInPanel.add(taskListPanel);
    add(loggedInPanel, LOGGED_IN_CONSTRAINT);

    reload();
  }

  private static JPanel createLoggedOutPanel() {
    JPanel loggedOutPanel = new JPanel();
    loggedOutPanel.setLayout(new BoxLayout(loggedOutPanel, BoxLayout.X_AXIS));
    JLabel loggedOutLabel =
        new JLabel(
            "<html><body style='text-align:center'>Log in to start tracking"
                + " progress.</body></html>");
    loggedOutLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loggedOutLabel.setAlignmentY(Component.TOP_ALIGNMENT);
    loggedOutLabel.setFont(FontManager.getRunescapeSmallFont());
    loggedOutLabel.setForeground(ColorScheme.BRAND_ORANGE);
    loggedOutPanel.add(loggedOutLabel);
    return loggedOutPanel;
  }

  public void reload() {
    if (manager.getUsername() == null) {
      layout.show(this, LOGGED_OUT_CONSTRAINT);
    } else {
      layout.show(this, LOGGED_IN_CONSTRAINT);
      clogNotice.setVisible(!manager.isStoringClogData());
      taskListPanel.reload();
    }
    revalidate();
  }
}
