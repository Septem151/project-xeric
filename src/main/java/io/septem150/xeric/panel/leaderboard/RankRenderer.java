package io.septem150.xeric.panel.leaderboard;

import io.septem150.xeric.data.player.ClanRank;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.text.WordUtils;

public class RankRenderer extends JLabel implements TableCellRenderer {
  private final ClientThread clientThread;
  private final SpriteManager spriteManager;
  private final Map<ClanRank, ImageIcon> imageMap = new HashMap<>();

  public RankRenderer(ClientThread clientThread, SpriteManager spriteManager) {
    this.clientThread = clientThread;
    this.spriteManager = spriteManager;
    clientThread.invokeLater(
        () -> {
          Arrays.stream(ClanRank.values())
              .forEach(
                  clanRank ->
                      imageMap.put(
                          clanRank,
                          new ImageIcon(
                              ImageUtil.resizeImage(
                                  clanRank.getImage(spriteManager), 14, 14, true))));
        });
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object clanRank, boolean isSelected, boolean hasFocus, int row, int col) {
    ClanRank rank = (ClanRank) clanRank;
    setText(String.valueOf(row + 1));
    setIcon(imageMap.get(rank));
    setToolTipText(WordUtils.capitalizeFully(rank.name()));
    setHorizontalAlignment(SwingConstants.LEFT);
    setHorizontalTextPosition(SwingConstants.LEFT);
    setBorder(new EmptyBorder(0, 3, 0, 0));
    return this;
  }
}
