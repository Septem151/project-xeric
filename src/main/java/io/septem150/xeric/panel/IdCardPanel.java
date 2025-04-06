package io.septem150.xeric.panel;

import io.septem150.xeric.data.PlayerInfo;
import java.awt.Color;
import java.awt.GridBagLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class IdCardPanel extends JPanel {
  private final PlayerInfo playerInfo;

  @Inject
  private IdCardPanel(PlayerInfo playerInfo) {
    super(new GridBagLayout());
    this.playerInfo = playerInfo;
    setBorder(new LineBorder(Color.RED, 1));
  }

  //  private JPanel createClanCardPanel() {
  //    JPanel clanCardPanel = new JPanel(new BorderLayout());
  //    clanCardPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    clanCardPanel.setBorder(
  //        BorderFactory.createCompoundBorder(
  //            BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
  //            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
  //    JLabel rankIcon = createRankIcon(manager.getRank());
  //    clanCardPanel.add(rankIcon, BorderLayout.WEST);
  //    JPanel playerInfoPanel = createPlayerInfoPanel();
  //    clanCardPanel.add(playerInfoPanel, BorderLayout.CENTER);
  //    JPanel valuePanel = createValuePanel(String.valueOf(manager.getPoints()), "Points");
  //    clanCardPanel.add(valuePanel, BorderLayout.EAST);
  //    JPanel numbersPanel = new JPanel();
  //    numbersPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));
  //    numbersPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
  //    JPanel row1 = new JPanel();
  //    row1.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
  //    row1.setBorder(new EmptyBorder(0, 0, 10, 0));
  //    row1.add(createValuePanel(String.valueOf(manager.getTasksCompleted()), "Tasks completed"));
  //    row1.add(Box.createHorizontalGlue());
  //    row1.add(
  //        createValuePanel(String.valueOf(manager.getPointsToNextRank()), "Points to next rank"));
  //    numbersPanel.add(row1);
  //    JPanel row2 = new JPanel();
  //    row2.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
  //    row2.add(createValuePanel(manager.getHighestTierCompleted(), "Highest tier completed"));
  //    numbersPanel.add(row2);
  //    clanCardPanel.add(numbersPanel, BorderLayout.SOUTH);
  //    return clanCardPanel;
  //  }
  //
  //  private JLabel createRankIcon(ClanRank clanRank) {
  //    JLabel rankIcon = new JLabel(new ImageIcon(clanRank.getImage(spriteManager)));
  //    rankIcon.setToolTipText(clanRank.toString());
  //    return rankIcon;
  //  }
  //
  //  private JPanel createPlayerInfoPanel() {
  //    JPanel playerInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
  //    playerInfoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    playerInfoPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
  //    JLabel usernameLabel =
  //        new JLabel(
  //            manager.getUsername(),
  //            new ImageIcon(manager.getAccountTypeImage()),
  //            SwingConstants.LEFT);
  //    usernameLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 14));
  //    usernameLabel.setForeground(Color.WHITE);
  //    usernameLabel.setIconTextGap(4);
  //    playerInfoPanel.add(usernameLabel);
  //    JPanel exceptionsPanel = new JPanel();
  //    exceptionsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    exceptionsPanel.setLayout(new BoxLayout(exceptionsPanel, BoxLayout.X_AXIS));
  //    JLabel slayerIcon = createExceptionIcon("Off-Island Slayer", "slayer_icon.png");
  //    slayerIcon.setEnabled(manager.isOffIslandSlayerUnlocked());
  //    exceptionsPanel.add(slayerIcon);
  //    JLabel herbloreIcon = createExceptionIcon("Herblore Access", "herblore_icon.png");
  //    herbloreIcon.setEnabled(manager.isHerbloreUnlocked());
  //    exceptionsPanel.add(herbloreIcon);
  //    JLabel boxTrapIcon = createExceptionIcon("Box Trap Access", "box_trap_icon.png");
  //    boxTrapIcon.setEnabled(manager.isBoxTrapUnlocked());
  //    exceptionsPanel.add(boxTrapIcon);
  //    playerInfoPanel.add(exceptionsPanel);
  //    return playerInfoPanel;
  //  }
  //
  //  private JLabel createExceptionIcon(String tooltip, String imageName) {
  //    BufferedImage image = ResourceUtil.getImage(imageName, 18, 18);
  //    JLabel exceptionIcon = new JLabel(new ImageIcon(image));
  //    exceptionIcon.setToolTipText(tooltip);
  //    exceptionIcon.setAlignmentY(Component.BOTTOM_ALIGNMENT);
  //    exceptionIcon.setBorder(new EmptyBorder(0, 0, 0, 5));
  //    exceptionIcon.setDisabledIcon(
  //        new ImageIcon(
  //            ImageUtil.alphaOffset(
  //                ImageUtil.recolorImage(image, ColorScheme.MEDIUM_GRAY_COLOR), 0.5F)));
  //    return exceptionIcon;
  //  }
  //
  //  private JPanel createValuePanel(String value, String description) {
  //    JPanel valuePanel = new JPanel();
  //    valuePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
  //    valuePanel.setLayout(new GridLayout(2, 1));
  //    JLabel valueLabel = new JLabel(value);
  //    valueLabel.setForeground(Color.WHITE);
  //    valueLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 12));
  //    valueLabel.setOpaque(true);
  //    valueLabel.setBackground(ColorScheme.BORDER_COLOR);
  //    valueLabel.setBorder(new EmptyBorder(3, 5, 3, 5));
  //    valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
  //    valuePanel.add(valueLabel);
  //    JLabel descriptionLabel = new JLabel(description);
  //    descriptionLabel.setForeground(ColorScheme.TEXT_COLOR);
  //    descriptionLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.PLAIN, 10));
  //    descriptionLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
  //    descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
  //    valuePanel.add(descriptionLabel);
  //    return valuePanel;
  //  }
}
