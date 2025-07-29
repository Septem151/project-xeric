package io.septem150.xeric.panel.leaderboard;

import javax.swing.table.AbstractTableModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class HiscoresTableModel extends AbstractTableModel {
  private static final String[] columnNames = {"Rank", "Username", "Type", "Points"};

  private Object[][] data;

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int col) {
    return columnNames[col];
  }

  @Override
  public Object getValueAt(int row, int col) {
    return data[row][col];
  }

  @Override
  public Class<?> getColumnClass(int col) {
    log.debug("Getting column class: {} {}", col, getValueAt(0, col).getClass());
    return getValueAt(0, col).getClass();
  }

  @Override
  public void setValueAt(Object value, int row, int col) {
    data[row][col] = value;
    fireTableCellUpdated(row, col);
  }
}
