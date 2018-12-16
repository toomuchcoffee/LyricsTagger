package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class Table extends JTable {
    private static final int COL_STATUS = 5;
    private static final int COL_LYRICS = 6;

    Table(Main main) {
        super();

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                AudioFileRecord.Status status = (AudioFileRecord.Status) table.getModel().getValueAt(row, COL_STATUS);

                switch (status) {
                    case LYRICS_FOUND:
                        setBackground(Color.YELLOW);
                        setToolTipText((String) table.getModel().getValueAt(row, COL_LYRICS));
                        break;
                    case LYRICS_WRITTEN:
                        setBackground(Color.GREEN);
                        break;
                    case LYRICS_NOT_FOUND:
                        setBackground(Color.LIGHT_GRAY);
                        break;
                    case INITIAL:
                    default:
                        setBackground(table.getBackground());
                }
                return this;
            }
        });

        setModel(new DefaultTableModel() {
            private String[] columns = new String[]{"Nr", "Artist", "Album", "Title", "File"};

            public int getColumnCount() {
                return columns.length;
            }

            public int getRowCount() {
                return main.getRecords().size();
            }

            public Object getValueAt(int row, int col) {
                switch (col) {
                    case 0:
                        return row + 1;
                    case 1:
                        return main.getRecords().get(row).getArtist();
                    case 2:
                        return main.getRecords().get(row).getAlbum();
                    case 3:
                        return main.getRecords().get(row).getTitle();
                    case 4:
                        return main.getRecords().get(row).getFile().getName();
                    case 5:
                        return main.getRecords().get(row).getStatus();
                    case 6:
                        return "<html>" + main.getRecords().get(row).getLyrics().replaceAll("\n", "<br/>") + "</html>";
                    default:
                        return null;
                }
            }

            public String getColumnName(int col) {
                return columns[col];
            }
        });
    }
}
