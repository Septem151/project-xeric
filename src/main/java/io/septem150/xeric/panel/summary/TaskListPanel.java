package io.septem150.xeric.panel.summary;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.task.TaskBase;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

@Singleton
public class TaskListPanel extends JPanel {
  private final CardLayout displayLayout = new CardLayout();

  private final Map<Integer, JPanel> tierPanels = new HashMap<>();
  private final Map<Integer, List<TaskBase>> tasksPerTier = new HashMap<>();

  private final SpriteManager spriteManager;
  private final ClientThread clientThread;
  private final PlayerData playerData;

  private boolean loaded;

  @Inject
  private TaskListPanel(
      SpriteManager spriteManager, ClientThread clientThread, PlayerData playerData) {
    this.spriteManager = spriteManager;
    this.clientThread = clientThread;
    this.playerData = playerData;
    loaded = false;
  }

  private final JPanel display = new JPanel(displayLayout);
  private final JComboBox<String> tierComboBox = new JComboBox<>();
  private final JScrollPane scrollPane =
      new JScrollPane(
          display,
          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

  private void makeLayout() {
    removeAll();
    setLayout(new BorderLayout());
    tierComboBox.setBorder(new LineBorder(ColorScheme.BORDER_COLOR, 1));
    add(tierComboBox, BorderLayout.NORTH);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setBorder(new EmptyBorder(5, 0, 0, 0));
    add(scrollPane, BorderLayout.CENTER);
  }

  private void init() {
    if (loaded) return;
    loaded = true;
    tierComboBox.setModel(
        new DefaultComboBoxModel<>(
            tasksPerTier.keySet().stream()
                .sorted()
                .map(TaskListPanel::tierToDropdownName)
                .toArray(String[]::new)));
    tierComboBox.addActionListener(
        event -> {
          String tierSelected =
              Objects.requireNonNull((String) ((JComboBox<?>) event.getSource()).getSelectedItem());
          scrollPane.getVerticalScrollBar().setValue(0);
          displayLayout.show(display, tierSelected);
        });
    for (int tier : tasksPerTier.keySet()) {
      JPanel taskList = new JPanel();
      taskList.setLayout(new BoxLayout(taskList, BoxLayout.Y_AXIS));
      taskList.setBorder(new EmptyBorder(0, 0, 0, 5));
      tierPanels.put(tier, taskList);
      display.add(taskList, tierToDropdownName(tier));
    }
  }

  private static String tierToDropdownName(int tier) {
    return "Tier " + tier + " Tasks (" + tier + " point" + (tier > 1 ? "s" : "") + " each)";
  }

  public void refresh(Map<Integer, TaskBase> allTasks) {
    makeLayout();
    if (!playerData.isLoggedIn()) return;
    tasksPerTier.clear();
    for (TaskBase task : allTasks.values()) {
      List<TaskBase> tasksInTier = tasksPerTier.getOrDefault(task.getTier(), new ArrayList<>());
      tasksInTier.add(task);
      tasksPerTier.put(task.getTier(), tasksInTier);
    }
    if (!loaded) {
      init();
    }
    for (Entry<Integer, JPanel> entry : tierPanels.entrySet()) {
      int tier = entry.getKey();
      JPanel panel = entry.getValue();
      panel.removeAll();
      List<TaskBase> tasks = tasksPerTier.get(tier);
      tasks.sort(Comparator.comparing(TaskBase::getType).thenComparing(TaskBase::getName));
      for (int i = 0; i < tasks.size(); i++) {
        TaskBase task = tasks.get(i);
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.X_AXIS));
        taskPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        taskPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel taskLabel = new JLabel(task.getName());
        taskLabel.setHorizontalAlignment(SwingConstants.LEFT);
        clientThread.invokeLater(
            () -> taskLabel.setIcon(new ImageIcon(task.getIcon(spriteManager))));
        taskPanel.add(taskLabel);
        taskPanel.add(Box.createHorizontalGlue());
        JCheckBox completedCheckbox = new JCheckBox();
        completedCheckbox.setEnabled(false);
        completedCheckbox.setSelected(playerData.getTasks().contains(task));
        completedCheckbox.setBorder(new EmptyBorder(0, 5, 0, 0));
        taskPanel.add(completedCheckbox);
        panel.add(taskPanel);
        if (i < tasks.size() - 1) {
          panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
      }
    }
    revalidate();
  }
}
