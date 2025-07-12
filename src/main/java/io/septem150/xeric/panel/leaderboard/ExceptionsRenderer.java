package io.septem150.xeric.panel.leaderboard;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

public class ExceptionsRenderer extends JPanel implements TableCellRenderer {
  private final ClientThread clientThread;
  private final SpriteManager spriteManager;
  private final Map<String, ImageIcon> imageMap = new HashMap<>();

  public ExceptionsRenderer(ClientThread clientThread, SpriteManager spriteManager) {
    this.clientThread = clientThread;
    this.spriteManager = spriteManager;
    clientThread.invokeLater(
        () -> {
          imageMap.put(
              "Slayer",
              new ImageIcon(
                  ImageUtil.resizeImage(
                      Objects.requireNonNull(spriteManager.getSprite(216, 0)), 14, 14, true)));
          imageMap.put(
              "Other",
              new ImageIcon(
                  ImageUtil.resizeImage(
                      Objects.requireNonNull(spriteManager.getSprite(3196, 0)), 14, 14, true)));
        });
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(false);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object exceptions, boolean isSelected, boolean hasFocus, int row, int col) {
    List<String> newExceptions = (List<String>) exceptions;
    removeAll();
    for (int i = 0; i < newExceptions.size(); i++) {
      String exc = newExceptions.get(i);
      if (imageMap.containsKey(exc)) {
        JLabel label = new JLabel();
        label.setIcon(imageMap.get(exc));
        label.setToolTipText(exc);
        add(label);
        if (i < newExceptions.size() - 1) {
          add(Box.createRigidArea(new Dimension(2, 0)));
        }
      }
    }
    setBorder(new EmptyBorder(0, 2, 0, 0));
    setAlignmentX(Component.CENTER_ALIGNMENT);
    return this;
  }
}
