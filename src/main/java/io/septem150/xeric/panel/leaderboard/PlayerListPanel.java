package io.septem150.xeric.panel.leaderboard;

import com.google.gson.Gson;
import io.septem150.xeric.data.hiscore.Hiscore;
import io.septem150.xeric.data.hiscore.HiscoreStore;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
public class PlayerListPanel extends JPanel {
  private static final String REGULAR_HISCORES = "Regular Hiscores";
  private static final String SLAYER_HISCORES = "Slayer Hiscores";

  private final CardLayout displayLayout = new CardLayout();

  private final HiscoreStore hiscoreStore;
  private final SpriteManager spriteManager;
  private final Gson gson;

  private boolean loaded;
  private List<Hiscore> nonSlayerHiscores;
  private List<Hiscore> slayerHiscores;

  @Inject
  private PlayerListPanel(
      HiscoreStore hiscoreStore, SpriteManager spriteManager, @Named("xericGson") Gson gson) {
    this.hiscoreStore = hiscoreStore;
    this.spriteManager = spriteManager;
    this.gson = gson;
    loaded = false;
  }

  private final JPanel display = new JPanel(displayLayout);
  private final JComboBox<String> hiscoresComboBox = new JComboBox<>();
  private final JPanel slayerList = new JPanel();
  private final JPanel regularList = new JPanel();

  private void makeLayout() {
    removeAll();
    setLayout(new BorderLayout());
    hiscoresComboBox.setBorder(new LineBorder(ColorScheme.BORDER_COLOR, 1));
    add(hiscoresComboBox, BorderLayout.NORTH);
    JScrollPane scrollPane =
        new JScrollPane(
            display,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setBorder(new EmptyBorder(5, 0, 0, 0));
    add(scrollPane, BorderLayout.CENTER);
  }

  private void makeStaticData() {}

  private void makeDynamicData() {}

  public void init() {
    if (loaded) return;
    loaded = true;
    loadHiscores();
    hiscoresComboBox.setModel(
        new DefaultComboBoxModel<>(new String[] {REGULAR_HISCORES, SLAYER_HISCORES}));
    hiscoresComboBox.addActionListener(
        event -> {
          String hiscoresSelected =
              Objects.requireNonNull((String) ((JComboBox<?>) event.getSource()).getSelectedItem());
          displayLayout.show(display, hiscoresSelected);
        });
    regularList.setLayout(new BoxLayout(regularList, BoxLayout.Y_AXIS));
    regularList.setBorder(new EmptyBorder(0, 0, 0, 5));
    display.add(regularList, REGULAR_HISCORES);

    slayerList.setLayout(new BoxLayout(slayerList, BoxLayout.Y_AXIS));
    slayerList.setBorder(new EmptyBorder(0, 0, 0, 5));
    display.add(slayerList, SLAYER_HISCORES);
    reload();
  }

  public void loadHiscores() {
    slayerHiscores = new ArrayList<>();
    nonSlayerHiscores = new ArrayList<>();
    for (Hiscore hiscore : hiscoreStore.getAll()) {
      if (hiscore.isSlayerException()) {
        slayerHiscores.add(hiscore);
      } else {
        nonSlayerHiscores.add(hiscore);
      }
    }
    slayerHiscores =
        slayerHiscores.stream()
            .sorted(Comparator.comparingInt(Hiscore::getPoints).reversed())
            .collect(Collectors.toList());
    nonSlayerHiscores =
        nonSlayerHiscores.stream()
            .sorted(Comparator.comparingInt(Hiscore::getPoints).reversed())
            .collect(Collectors.toList());
    log.debug("Non-Slayer Hiscores:\n{}", gson.toJson(nonSlayerHiscores));
    log.debug("Slayer Hiscores:\n{}", gson.toJson(slayerHiscores));
  }

  private JPanel createHiscorePanel(int rank, Hiscore hiscore) {
    JPanel hiscorePanel = new JPanel();
    hiscorePanel.setLayout(new BoxLayout(hiscorePanel, BoxLayout.X_AXIS));
    hiscorePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    hiscorePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    JLabel rankLabel = new JLabel(String.valueOf(rank));
    rankLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    hiscore
        .getRank()
        .getImageAsync(
            spriteManager,
            image -> rankLabel.setIcon(new ImageIcon(ImageUtil.resizeImage(image, 16, 16))));
    hiscorePanel.add(rankLabel);
    hiscorePanel.add(Box.createRigidArea(new Dimension(5, 0)));
    JLabel usernameLabel = new JLabel(hiscore.getUsername());
    usernameLabel.setHorizontalAlignment(SwingConstants.LEFT);
    hiscore
        .getAccountType()
        .getImageAsync(
            spriteManager,
            image -> usernameLabel.setIcon(new ImageIcon(ImageUtil.resizeImage(image, 12, 14))));
    hiscorePanel.add(usernameLabel);
    hiscorePanel.add(Box.createHorizontalGlue());
    JLabel pointsLabel = new JLabel(String.format("%d Points", hiscore.getPoints()));
    hiscorePanel.add(pointsLabel);
    return hiscorePanel;
  }

  public void reload() {
    makeLayout();
    makeStaticData();
    makeDynamicData();
    if (!loaded) {
      init();
    }
    regularList.removeAll();
    for (int i = 0; i < nonSlayerHiscores.size(); i++) {
      Hiscore hiscore = nonSlayerHiscores.get(i);
      JPanel hiscorePanel = createHiscorePanel(i + 1, hiscore);
      regularList.add(hiscorePanel);
      if (i < nonSlayerHiscores.size() - 1) {
        regularList.add(Box.createRigidArea(new Dimension(0, 5)));
      }
    }
    slayerList.removeAll();
    for (int i = 0; i < slayerHiscores.size(); i++) {
      Hiscore hiscore = slayerHiscores.get(i);
      JPanel hiscorePanel = createHiscorePanel(i + 1, hiscore);
      slayerList.add(hiscorePanel);
      if (i < slayerHiscores.size() - 1) {
        slayerList.add(Box.createRigidArea(new Dimension(0, 5)));
      }
    }
    revalidate();
  }
}
