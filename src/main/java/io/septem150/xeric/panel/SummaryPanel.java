package io.septem150.xeric.panel;

import io.septem150.xeric.ProjectXericManager;
import io.septem150.xeric.data.player.ClanRank;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;

@Singleton
public final class SummaryPanel extends PanelBase {
  public static final String TOOLTIP = "Player Summary";
  public static final String TAB_ICON = "summary_tab_icon.png";

  private final ProjectXericManager manager;

  @Inject
  private SummaryPanel(Client client, ProjectXericManager manager) {
    super();
    this.manager = manager;
    init();
  }

  private void init() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    if (manager.getUsername() == null) {
      JLabel description = new JShadowedLabel();
      description.setFont(FontManager.getRunescapeSmallFont());
      description.setForeground(Color.GRAY);
      description.setHorizontalAlignment(SwingConstants.CENTER);
      description.setText("<html>Log in to start tracking progress.");
      description.setBorder(new EmptyBorder(20, 0, 0, 0));
      add(description);
    } else {
      add(createClanCardPanel());
      add(Box.createVerticalStrut(10));
      if (!manager.isStoringClogData()) {
        JLabel collectionLogNotice = new JShadowedLabel();
        collectionLogNotice.setFont(FontManager.getRunescapeSmallFont());
        collectionLogNotice.setForeground(ColorScheme.BRAND_ORANGE);
        collectionLogNotice.setAlignmentX(Component.CENTER_ALIGNMENT);
        collectionLogNotice.setHorizontalAlignment(SwingConstants.CENTER);
        collectionLogNotice.setText("<html>Open the Collection Log in-game to sync.");
        add(collectionLogNotice);
        add(Box.createVerticalStrut(10));
      }
      add(createTaskList());
    }
  }

  private JScrollPane createTaskList() {
    JPanel taskListPanel = new JPanel(new GridLayout(0, 1, 0, 4));
    taskListPanel.setBorder(new EmptyBorder(0, 0, 0, 4));
    for (Task task : manager.getAllTasks()) {
      taskListPanel.add(createTask(task));
    }
    JScrollPane scrollPane =
        new JScrollPane(
            taskListPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
    return scrollPane;
  }

  private JPanel createTask(Task task) {
    JPanel taskPanel = new JPanel(new BorderLayout());
    JLabel icon =
        new JLabel(
            task.getName(),
            new ImageIcon(ResourceUtil.getImage(task.getIcon(), 24, 24)),
            SwingConstants.CENTER);
    taskPanel.add(icon, BorderLayout.WEST);
    JPanel labeledCheckbox = new JPanel();
    labeledCheckbox.setLayout(new BoxLayout(labeledCheckbox, BoxLayout.X_AXIS));
    JLabel checkboxLabel = new JLabel(String.format("+%d", task.getTier()));
    JCheckBox checkbox = new JCheckBox();
    checkbox.setEnabled(false);
    checkbox.setSelected(manager.isTaskCompleted(task));
    labeledCheckbox.add(checkboxLabel);
    labeledCheckbox.add(Box.createHorizontalStrut(4));
    labeledCheckbox.add(checkbox);
    taskPanel.add(labeledCheckbox, BorderLayout.EAST);
    return taskPanel;
  }

  private JPanel createClanCardPanel() {
    JPanel clanCardPanel = new JPanel(new BorderLayout());
    clanCardPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    clanCardPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    JLabel rankIcon = createRankIcon(manager.getRank());
    clanCardPanel.add(rankIcon, BorderLayout.WEST);
    JPanel playerInfoPanel = createPlayerInfoPanel();
    clanCardPanel.add(playerInfoPanel, BorderLayout.CENTER);
    JPanel valuePanel = createValuePanel(String.valueOf(manager.getPoints()), "Points");
    clanCardPanel.add(valuePanel, BorderLayout.EAST);
    JPanel numbersPanel = new JPanel();
    numbersPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));
    numbersPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
    JPanel row1 = new JPanel();
    row1.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
    row1.setBorder(new EmptyBorder(0, 0, 10, 0));
    row1.add(createValuePanel(String.valueOf(manager.getTasksCompleted()), "Tasks completed"));
    row1.add(Box.createHorizontalGlue());
    row1.add(
        createValuePanel(String.valueOf(manager.getPointsToNextRank()), "Points to next rank"));
    numbersPanel.add(row1);
    JPanel row2 = new JPanel();
    row2.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
    row2.add(createValuePanel(manager.getHighestTierCompleted(), "Highest tier completed"));
    numbersPanel.add(row2);
    clanCardPanel.add(numbersPanel, BorderLayout.SOUTH);
    return clanCardPanel;
  }

  private JLabel createRankIcon(ClanRank clanRank) {
    JLabel rankIcon = new JLabel(new ImageIcon(clanRank.getImage()));
    rankIcon.setToolTipText(clanRank.toString());
    return rankIcon;
  }

  private JPanel createPlayerInfoPanel() {
    JPanel playerInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    playerInfoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    playerInfoPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
    JLabel usernameLabel =
        new JLabel(
            manager.getUsername(),
            new ImageIcon(manager.getAccountTypeImage()),
            SwingConstants.LEFT);
    usernameLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 14));
    usernameLabel.setForeground(Color.WHITE);
    usernameLabel.setIconTextGap(4);
    playerInfoPanel.add(usernameLabel);
    JPanel exceptionsPanel = new JPanel();
    exceptionsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    exceptionsPanel.setLayout(new BoxLayout(exceptionsPanel, BoxLayout.X_AXIS));
    JLabel slayerIcon = createExceptionIcon("Off-Island Slayer", "slayer_icon.png");
    slayerIcon.setEnabled(manager.isOffIslandSlayerUnlocked());
    exceptionsPanel.add(slayerIcon);
    JLabel herbloreIcon = createExceptionIcon("Herblore Access", "herblore_icon.png");
    herbloreIcon.setEnabled(manager.isHerbloreUnlocked());
    exceptionsPanel.add(herbloreIcon);
    JLabel boxTrapIcon = createExceptionIcon("Box Trap Access", "box_trap_icon.png");
    boxTrapIcon.setEnabled(manager.isBoxTrapUnlocked());
    exceptionsPanel.add(boxTrapIcon);
    playerInfoPanel.add(exceptionsPanel);
    return playerInfoPanel;
  }

  private JLabel createExceptionIcon(String tooltip, String imageName) {
    BufferedImage image = ResourceUtil.getImage(imageName, 18, 18);
    JLabel exceptionIcon = new JLabel(new ImageIcon(image));
    exceptionIcon.setToolTipText(tooltip);
    exceptionIcon.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    exceptionIcon.setBorder(new EmptyBorder(0, 0, 0, 5));
    exceptionIcon.setDisabledIcon(
        new ImageIcon(
            ImageUtil.alphaOffset(
                ImageUtil.recolorImage(image, ColorScheme.MEDIUM_GRAY_COLOR), 0.5F)));
    return exceptionIcon;
  }

  private JPanel createValuePanel(String value, String description) {
    JPanel valuePanel = new JPanel();
    valuePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    valuePanel.setLayout(new GridLayout(2, 1));
    JLabel valueLabel = new JLabel(value);
    valueLabel.setForeground(Color.WHITE);
    valueLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 12));
    valueLabel.setOpaque(true);
    valueLabel.setBackground(ColorScheme.BORDER_COLOR);
    valueLabel.setBorder(new EmptyBorder(3, 5, 3, 5));
    valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
    valuePanel.add(valueLabel);
    JLabel descriptionLabel = new JLabel(description);
    descriptionLabel.setForeground(ColorScheme.TEXT_COLOR);
    descriptionLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.PLAIN, 10));
    descriptionLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
    descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
    valuePanel.add(descriptionLabel);
    return valuePanel;
  }

  @Override
  public void reload() {
    removeAll();
    init();
    revalidate();
  }
}
