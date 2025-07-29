package io.septem150.xeric.panel.leaderboard;

import io.septem150.xeric.data.player.AccountType;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

public class UsernameRenderer extends JLabel implements TableCellRenderer {
  private final ClientThread clientThread;
  private final SpriteManager spriteManager;
  private final Map<AccountType, ImageIcon> imageMap = new HashMap<>();

  public UsernameRenderer(ClientThread clientThread, SpriteManager spriteManager) {
    this.clientThread = clientThread;
    this.spriteManager = spriteManager;
    clientThread.invokeLater(
        () -> {
          Arrays.stream(AccountType.values())
              .forEach(
                  accountType -> {
                    imageMap.put(
                        accountType,
                        new ImageIcon(
                            ImageUtil.resizeImage(
                                accountType.getImage(spriteManager), 14, 14, true)));
                  });
        });
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object username, boolean isSelected, boolean hasFocus, int row, int col) {
    Username newUsername = (Username) username;
    setIcon(imageMap.get(newUsername.getAccountType()));
    setText(newUsername.getUsername());
    setBorder(new EmptyBorder(0, 2, 0, 0));
    return this;
  }
}
