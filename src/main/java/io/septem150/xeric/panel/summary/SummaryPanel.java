package io.septem150.xeric.panel.summary;

import io.septem150.xeric.data.ProjectXericManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
@Singleton
public class SummaryPanel extends JPanel {
  public static final String TOOLTIP = "Player Summary";
  public static final String TAB_ICON = "summary_tab_icon.png";

  private final ProjectXericManager manager;
  private final TaskListPanel taskListPanel;
  private final IdCard idCardPanel;

  @Inject
  private SummaryPanel(
      ProjectXericManager manager, TaskListPanel taskListPanel, IdCard idCardPanel) {
    this.manager = manager;
    this.taskListPanel = taskListPanel;
    this.idCardPanel = idCardPanel;
  }

  private static final String LOGGED_OUT_CONSTRAINT = "LOGGED_OUT";
  private static final String LOGGED_IN_CONSTRAINT = "LOGGED_IN";
  private final CardLayout layout = new CardLayout();
  private final JLabel loginLabel = new JLabel();
  private final JLabel clogLabel = new JLabel();
  private final JPanel loggedOutPanel = new JPanel();
  private final JPanel loggedInPanel = new JPanel();
  private final JPanel taskListContainerPanel = new JPanel();

  private boolean loaded;

  private void initComponent() {
    loginLabel.setText(
        "<html><body style='text-align: center'>Log in to start tracking Xeric"
            + " Tasks.</body></html>");
    loginLabel.setFont(FontManager.getRunescapeSmallFont());
    loginLabel.setForeground(ColorScheme.BRAND_ORANGE);
    loginLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
    loginLabel.setHorizontalAlignment(SwingConstants.CENTER);

    clogLabel.setText(
        "<html><body style='text-align: center'>Open the Collection Log to start tracking Xeric"
            + " Tasks.</body></html>");
    clogLabel.setFont(FontManager.getRunescapeSmallFont());
    clogLabel.setForeground(ColorScheme.BRAND_ORANGE);
    clogLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
    clogLabel.setHorizontalAlignment(SwingConstants.CENTER);

    loggedOutPanel.setLayout(new BorderLayout());
    loggedOutPanel.add(loginLabel, BorderLayout.NORTH);

    loggedInPanel.setLayout(new BorderLayout());
    loggedInPanel.add(idCardPanel, BorderLayout.NORTH);
    loggedInPanel.add(taskListContainerPanel, BorderLayout.CENTER);

    taskListContainerPanel.setLayout(new BorderLayout());
    taskListContainerPanel.add(clogLabel, BorderLayout.NORTH);
    taskListContainerPanel.add(taskListPanel, BorderLayout.CENTER);

    setLayout(layout);
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    add(loggedOutPanel, LOGGED_OUT_CONSTRAINT);
    add(loggedInPanel, LOGGED_IN_CONSTRAINT);
  }

  public void startUp() {
    if (!loaded) {
      removeAll();
      initComponent();
      loaded = true;
    }
    refresh();
  }

  public void startupChildren() {
    taskListPanel.startUp();
  }

  public void refresh() {
    if (manager.getPlayerInfo().getUsername() == null) {
      layout.show(this, LOGGED_OUT_CONSTRAINT);
    } else {
      layout.show(this, LOGGED_IN_CONSTRAINT);
      clogLabel.setVisible(manager.getPlayerInfo().getCollectionLog().getLastOpened() == null);
      idCardPanel.reload();
      taskListPanel.reload();
    }
    revalidate();
  }
}
