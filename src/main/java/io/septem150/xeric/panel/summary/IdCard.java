package io.septem150.xeric.panel.summary;

import com.google.common.collect.Iterables;
import io.septem150.xeric.data.ProjectXericManager;
import io.septem150.xeric.data.player.ClanRank;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.panel.JLabeledValue;
import io.septem150.xeric.util.ResourceUtil;
import io.septem150.xeric.util.TransferableBufferedImage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import org.apache.commons.text.WordUtils;

@Singleton
public class IdCard extends JPanel {
  private final ProjectXericManager manager;
  private final TaskStore taskStore;
  private final SpriteManager spriteManager;

  @Inject
  private IdCard(ProjectXericManager manager, TaskStore taskStore, SpriteManager spriteManager) {
    this.manager = manager;
    this.taskStore = taskStore;
    this.spriteManager = spriteManager;

    add(wrappedPanel, BorderLayout.NORTH);

    SwingUtil.removeButtonDecorations(screenshotButton);
    screenshotButton.setToolTipText("Copy ID Card to your clipboard");
    screenshotButton.setUI(new BasicButtonUI());
    screenshotButton.setBorder(new EmptyBorder(0, 0, 0, 0));
    screenshotButton.setFocusable(false);
    screenshotButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    screenshotButton.addActionListener(
        actionEvent -> {
          BufferedImage image =
              new BufferedImage(
                  wrappedPanel.getWidth(), wrappedPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
          screenshotButton.setVisible(false);
          wrappedPanel.paint(image.getGraphics());
          screenshotButton.setVisible(true);
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          TransferableBufferedImage transferableImage = new TransferableBufferedImage(image);
          clipboard.setContents(transferableImage, null);
        });
    screenshotButton.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            screenshotButton.setBorder(new EmptyBorder(0, 0, 0, 0));
          }

          @Override
          public void mouseExited(MouseEvent e) {
            screenshotButton.setBorder(new EmptyBorder(0, 0, 0, 0));
          }
        });
  }

  private final JPanel wrappedPanel = new JPanel(new GridBagLayout());
  private final JLabel rank = new JLabel();
  private final JLabel username = new JLabel();
  private final JLabeledValue points = new JLabeledValue();
  private final JLabel herbException = new JLabel();
  private final JLabel chinException = new JLabel();
  private final JLabel slayException = new JLabel();
  private final JLabeledValue tasksCompleted = new JLabeledValue();
  private final JLabeledValue pointsToNextRank = new JLabeledValue();
  private final JLabeledValue highestTierCompleted = new JLabeledValue();
  private final JButton screenshotButton = new JButton();

  private void makeLayout() {
    removeAll();
    wrappedPanel.removeAll();
    wrappedPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    wrappedPanel.setBorder(new LineBorder(ColorScheme.BORDER_COLOR, 1));
    setLayout(new BorderLayout());
    rank.setPreferredSize(new Dimension(32, 32));
    wrappedPanel.add(rank, gbc(0, 0, 1, 2, 0, GridBagConstraints.CENTER, new Insets(5, 10, 5, 5)));

    username.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 14));
    wrappedPanel.add(username, gbc(1, 0, 2, 1, 1, GridBagConstraints.WEST, new Insets(5, 0, 1, 0)));

    final JPanel exceptions = new JPanel();
    exceptions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    exceptions.setLayout(new BoxLayout(exceptions, BoxLayout.X_AXIS));
    herbException.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    chinException.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    slayException.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    exceptions.add(herbException);
    exceptions.add(chinException);
    exceptions.add(slayException);
    wrappedPanel.add(
        exceptions, gbc(1, 1, 2, 1, 1, GridBagConstraints.WEST, new Insets(1, 0, 5, 0)));

    wrappedPanel.add(
        points, gbc(3, 0, 1, 2, 0, GridBagConstraints.CENTER, new Insets(5, 5, 5, 10)));

    wrappedPanel.add(
        tasksCompleted, gbc(0, 2, 2, 1, 0, GridBagConstraints.WEST, new Insets(5, 10, 5, 5)));

    wrappedPanel.add(
        pointsToNextRank, gbc(2, 2, 2, 1, 0, GridBagConstraints.EAST, new Insets(5, 5, 5, 10)));

    wrappedPanel.add(
        highestTierCompleted,
        gbc(0, 3, 3, 1, 0, GridBagConstraints.CENTER, new Insets(5, 10, 5, 5)));

    wrappedPanel.add(
        screenshotButton, gbc(3, 3, 1, 1, 0, GridBagConstraints.CENTER, new Insets(5, 5, 5, 10)));
    add(wrappedPanel, BorderLayout.NORTH);
  }

  private GridBagConstraints gbc(
      int x, int y, int width, int height, double weightX, int anchor, Insets insets) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = width;
    gbc.gridheight = height;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = weightX;
    gbc.anchor = anchor;
    gbc.insets = insets;
    return gbc;
  }

  private void makeStaticData() {
    points.setLabel("Points");
    tasksCompleted.setLabel("Tasks completed");
    pointsToNextRank.setLabel("Points to next rank");
    highestTierCompleted.setLabel("Highest tier completed");
    screenshotButton.setIcon(
        new ImageIcon(
            ResourceUtil.getImage("/net/runelite/client/plugins/screenshot/screenshot.png")));
  }

  private void makeDynamicData() {
    PlayerInfo playerInfo = manager.getPlayerInfo();
    int playerPoints = playerInfo.getPoints();
    ClanRank playerRank = playerInfo.getRank();
    playerRank.getImageAsync(spriteManager, image -> rank.setIcon(new ImageIcon(image)));
    rank.setToolTipText(WordUtils.capitalizeFully(playerRank.name()));
    playerInfo
        .getAccountType()
        .getImageAsync(spriteManager, image -> username.setIcon(new ImageIcon(image)));
    username.setText(playerInfo.getUsername());
    points.setValue(playerPoints);
    if (herbException.getIcon() == null) {
      spriteManager.getSpriteAsync(
          205,
          0,
          image ->
              herbException.setIcon(new ImageIcon(ImageUtil.resizeImage(image, 20, 20, true))));
    }
    herbException.setEnabled(
        playerInfo.getQuests().stream()
            .anyMatch(
                qp ->
                    qp.getQuest() == Quest.DRUIDIC_RITUAL && qp.getState() == QuestState.FINISHED));
    if (chinException.getIcon() == null) {
      chinException.setIcon(
          new ImageIcon(ResourceUtil.getImage("box_trap_icon.png", 20, 20, true)));
    }
    chinException.setEnabled(
        playerInfo.getQuests().stream()
            .anyMatch(
                qp ->
                    qp.getQuest() == Quest.EAGLES_PEAK && qp.getState() != QuestState.NOT_STARTED));
    if (slayException.getIcon() == null) {
      spriteManager.getSpriteAsync(
          216,
          0,
          image ->
              slayException.setIcon(new ImageIcon(ImageUtil.resizeImage(image, 20, 20, true))));
    }
    slayException.setEnabled(playerInfo.isSlayerException());
    tasksCompleted.setValue(playerInfo.getTasks().size());
    pointsToNextRank.setValue(playerRank.getNextRank().getPointsNeeded() - playerPoints);
    highestTierCompleted.setValue(getHighestTierCompleted());
  }

  public void reload() {
    makeLayout();
    makeStaticData();
    makeDynamicData();
  }

  private String getHighestTierCompleted() {
    List<Task> tasks = manager.getAllTasks();
    List<Integer> tiers =
        tasks.stream().map(Task::getTier).distinct().sorted().collect(Collectors.toList());
    int highestTier = 0;
    int maxTiers = Optional.ofNullable(Iterables.getLast(tiers, 0)).orElse(0);
    for (int tier = 1; tier <= maxTiers; tier++) {
      if (manager.getPlayerInfo().getTasks().isEmpty()) break;
      boolean completed = true;
      for (Task task : tasks) {
        if (task.getTier() != tier) continue;
        if (!manager.getPlayerInfo().getTasks().contains(task)) {
          completed = false;
          break;
        }
      }
      if (completed) {
        highestTier = tier;
      } else break;
    }
    return highestTier > 0 ? String.format("Tier %d", highestTier) : "None";
  }
}
