package io.septem150.xeric.panel;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.panel.summary.IdCardPanel;
import io.septem150.xeric.panel.summary.TaskListPanel;
import io.septem150.xeric.task.TaskBase;
import java.awt.*;
import java.time.Instant;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
@Singleton
public class SummaryPanel extends JPanel {
  public static final String TOOLTIP = "Player Summary";
  public static final String TAB_ICON = "summary_tab_icon.png";

  private final TaskListPanel taskListPanel;
  private final IdCardPanel idCardPanel;
  private final PlayerData playerData;

  @Inject
  private SummaryPanel(
      TaskListPanel taskListPanel, IdCardPanel idCardPanel, PlayerData playerData) {
    this.taskListPanel = taskListPanel;
    this.idCardPanel = idCardPanel;
    this.playerData = playerData;
    makeLayout();
    makeStaticData();
  }

  private static final String LOGGED_OUT_CONSTRAINT = "LOGGED_OUT";
  private static final String LOGGED_IN_CONSTRAINT = "LOGGED_IN";
  private final CardLayout layout = new CardLayout();
  private final JLabel loginLabel = new JLabel();
  private final JLabel clogLabel = new JLabel();

  private void makeLayout() {
    removeAll();
    setLayout(layout);
    JPanel loggedOutPanel = new JPanel(new BorderLayout());
    loginLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
    loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loggedOutPanel.add(loginLabel, BorderLayout.NORTH);
    add(loggedOutPanel, LOGGED_OUT_CONSTRAINT);
    JPanel loggedInPanel = new JPanel(new BorderLayout());
    idCardPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
    loggedInPanel.add(idCardPanel, BorderLayout.NORTH);
    JPanel inner1 = new JPanel(new BorderLayout());
    clogLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
    clogLabel.setHorizontalAlignment(SwingConstants.CENTER);
    inner1.add(clogLabel, BorderLayout.NORTH);
    inner1.add(taskListPanel, BorderLayout.CENTER);
    loggedInPanel.add(inner1, BorderLayout.CENTER);
    add(loggedInPanel, LOGGED_IN_CONSTRAINT);
  }

  private void makeStaticData() {
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    loginLabel.setText(
        "<html><body style='text-align: center'>Log in to start tracking Xeric"
            + " Tasks.</body></html>");
    loginLabel.setFont(FontManager.getRunescapeSmallFont());
    loginLabel.setForeground(ColorScheme.BRAND_ORANGE);
    clogLabel.setText(
        "<html><body style='text-align: center'>Open the Collection Log to start tracking Xeric"
            + " Tasks.</body></html>");
    clogLabel.setFont(FontManager.getRunescapeSmallFont());
    clogLabel.setForeground(ColorScheme.BRAND_ORANGE);
  }

  public void refresh(Map<Integer, TaskBase> allTasks) {
    if (allTasks == null) return;
    makeLayout();
    makeStaticData();
    if (!playerData.isLoggedIn()) {
      layout.show(this, LOGGED_OUT_CONSTRAINT);
    } else {
      layout.show(this, LOGGED_IN_CONSTRAINT);
      clogLabel.setVisible(playerData.getCollectionLog().getLastUpdated().equals(Instant.EPOCH));
      idCardPanel.refresh(allTasks);
      taskListPanel.refresh(allTasks);
    }
    revalidate();
  }
}
