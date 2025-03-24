package io.septem150.xeric.panel;

import io.septem150.xeric.DataManager;
import io.septem150.xeric.util.ResourceManager;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

@Singleton
public final class SummaryPanel extends PanelBase {
  static final String TOOLTIP = "Player Summary";
  static final String TAB_ICON = "summary_tab_icon.png";

  private final DataManager dataManager;

  @Inject
  private SummaryPanel(DataManager dataManager) {
    super();
    this.dataManager = dataManager;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    init();
  }

  private void init() {
    if (!dataManager.isLoggedIn()) {
      PluginErrorPanel errorPanel = new PluginErrorPanel();
      errorPanel.setContent(TOOLTIP, "Log in to track progress");
      add(errorPanel);
    } else {
      JPanel clanCardPanel = createClanCardPanel();
      add(clanCardPanel);
    }
  }

  private JPanel createClanCardPanel() {
    JPanel clanCardPanel = new JPanel(new BorderLayout());
    clanCardPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    clanCardPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    JLabel rankIcon = createRankIcon("Emerald Rank", "emerald_rank_icon.png");
    clanCardPanel.add(rankIcon, BorderLayout.WEST);
    JPanel playerInfoPanel = createPlayerInfoPanel();
    clanCardPanel.add(playerInfoPanel, BorderLayout.CENTER);
    JPanel valuePanel = createValuePanel(String.valueOf(127), "Points");
    clanCardPanel.add(valuePanel, BorderLayout.EAST);
    JPanel numbersPanel = new JPanel();
    numbersPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));
    numbersPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
    JPanel row1 = new JPanel();
    row1.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
    row1.setBorder(new EmptyBorder(0, 0, 10, 0));
    row1.add(createValuePanel(String.valueOf(35), "Tasks completed"));
    row1.add(Box.createHorizontalGlue());
    row1.add(createValuePanel(String.valueOf(20), "Points to next rank"));
    numbersPanel.add(row1);
    JPanel row2 = new JPanel();
    row2.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
    row2.add(createValuePanel("Tier 3", "Highest tier completed"));
    numbersPanel.add(row2);
    clanCardPanel.add(numbersPanel, BorderLayout.SOUTH);
    return clanCardPanel;
  }

  private JLabel createRankIcon(String tooltip, String imageName) {
    JLabel rankIcon = new JLabel(new ImageIcon(ResourceManager.getImage(imageName, 32, 32)));
    rankIcon.setToolTipText(tooltip);
    return rankIcon;
  }

  private JPanel createPlayerInfoPanel() {
    JPanel playerInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    playerInfoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    playerInfoPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
    JLabel usernameLabel =
        new JLabel(
            dataManager.getUsername(),
            new ImageIcon(ResourceManager.getImage("hcim_icon.png", 14, 14, true)),
            SwingConstants.LEFT);
    usernameLabel.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 14));
    usernameLabel.setForeground(Color.WHITE);
    usernameLabel.setIconTextGap(4);
    playerInfoPanel.add(usernameLabel);
    JPanel exceptionsPanel = new JPanel();
    exceptionsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    exceptionsPanel.setLayout(new BoxLayout(exceptionsPanel, BoxLayout.X_AXIS));
    JLabel slayerIcon = createExceptionIcon("Off-Island Slayer", "slayer_icon.png");
    slayerIcon.setEnabled(dataManager.isSlayerException());
    exceptionsPanel.add(slayerIcon);
    JLabel herbloreIcon = createExceptionIcon("Herblore Access", "herblore_icon.png");
    herbloreIcon.setEnabled(dataManager.isHerbloreException());
    exceptionsPanel.add(herbloreIcon);
    JLabel boxTrapIcon = createExceptionIcon("Box Trap Access", "box_trap_icon.png");
    boxTrapIcon.setEnabled(dataManager.isBoxtrapException());
    exceptionsPanel.add(boxTrapIcon);
    playerInfoPanel.add(exceptionsPanel);
    return playerInfoPanel;
  }

  private JLabel createExceptionIcon(String tooltip, String imageName) {
    BufferedImage image = ResourceManager.getImage(imageName, 18, 18);
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
