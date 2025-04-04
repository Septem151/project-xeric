package io.septem150.xeric.panel;

import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.runelite.client.ui.ColorScheme;

public class TaskListPanel extends JPanel {
  private final CardLayout displayLayout = new CardLayout();
  private final JPanel display = new JPanel(displayLayout);
  private final JComboBox<String> tierComboBox = new JComboBox<>();
  private final Map<Integer, JPanel> tierPanels = new HashMap<>();
  private final Map<Integer, List<Task>> tasksPerTier = new HashMap<>();

  private final TaskStore taskStore;
  private final PlayerInfo playerInfo;

  private boolean loaded;

  @Inject
  private TaskListPanel(TaskStore taskStore, PlayerInfo playerInfo) {
    super(new BorderLayout());
    this.taskStore = taskStore;
    this.playerInfo = playerInfo;
    loaded = false;

    final JScrollPane scrollPane =
        new JScrollPane(
            display,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setBorder(new EmptyBorder(5, 0, 0, 0));

    tierComboBox.setBorder(new LineBorder(ColorScheme.BORDER_COLOR, 1));
    add(tierComboBox, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
  }

  public void init() {
    if (loaded) return;
    loaded = true;
    for (Task task : taskStore.getAll()) {
      List<Task> tasksInTier = tasksPerTier.getOrDefault(task.getTier(), new ArrayList<>());
      tasksInTier.add(task);
      tasksPerTier.put(task.getTier(), tasksInTier);
    }
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
          displayLayout.show(display, tierSelected);
        });
    for (int tier : tasksPerTier.keySet()) {
      JPanel taskList = new JPanel();
      taskList.setLayout(new BoxLayout(taskList, BoxLayout.Y_AXIS));
      taskList.setBorder(new EmptyBorder(0, 0, 0, 5));
      tierPanels.put(tier, taskList);
      display.add(taskList, tierToDropdownName(tier));
    }
    reload();
  }

  private static String tierToDropdownName(int tier) {
    return "Tier " + tier + " Tasks (" + tier + " point" + (tier > 1 ? "s" : "") + " each)";
  }

  public void reload() {
    if (playerInfo.getUsername() == null) return;
    if (!loaded) {
      init();
    }
    for (Entry<Integer, JPanel> entry : tierPanels.entrySet()) {
      int tier = entry.getKey();
      JPanel panel = entry.getValue();
      panel.removeAll();
      List<Task> tasks = tasksPerTier.get(tier);
      for (int i = 0; i < tasks.size(); i++) {
        Task task = tasks.get(i);
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.X_AXIS));
        taskPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        taskPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel taskLabel =
            new JLabel(
                task.getName(),
                new ImageIcon(ResourceUtil.getImage(task.getIcon(), 20, 20)),
                SwingConstants.LEFT);
        taskPanel.add(taskLabel);
        taskPanel.add(Box.createHorizontalGlue());
        JCheckBox completedCheckbox = new JCheckBox();
        completedCheckbox.setEnabled(false);
        completedCheckbox.setSelected(playerInfo.getTasks().contains(task));
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
