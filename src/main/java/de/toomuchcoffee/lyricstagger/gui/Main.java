package de.toomuchcoffee.lyricstagger.gui;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.toomuchcoffee.lyricstagger.core.CoreFacade;
import de.toomuchcoffee.lyricstagger.core.record.AudioFileRecord;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static de.toomuchcoffee.lyricstagger.gui.Step.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.*;
import static java.awt.event.ItemEvent.SELECTED;
import static java.lang.String.format;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;


@Slf4j
public class Main extends JFrame {

    private final CoreFacade core;

    private final Table table;

    private Step step = START;

    private Map<Step, JButton> buttons = ImmutableMap.of(
            READ_FILES, new JButton("Add music library path"),
            FIND_LYRICS, new JButton("Find lyrics"),
            WRITE_LYRICS, new JButton("Write lyrics"));

    private JCheckBox checkBox;

    @Inject
    public Main(CoreFacade core) {
        super("Add lyrics to your music library");

        this.core = core;

        setSize(1024, 768);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel north = new JPanel();
        panel.add(north, NORTH);

        checkBox = new JCheckBox("Overwrite existing lyrics", core.isFindAll());
        checkBox.addItemListener(e -> core.setFindAll(e.getStateChange() == SELECTED));
        north.add(checkBox);

        buttons.values().forEach(north::add);
        buttons.get(READ_FILES).addActionListener(e -> readFiles());
        buttons.get(FIND_LYRICS).addActionListener(e -> findLyrics());
        buttons.get(WRITE_LYRICS).addActionListener(e -> writeLyrics());

        next();

        table = new Table();
        panel.add(new JScrollPane(table), CENTER);

        getContentPane().add(panel);
    }

    private void readFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == APPROVE_OPTION) {
            File baseDir = fileChooser.getSelectedFile();
            Collection<File> files = listFiles(baseDir, null, true);

            process(files, core.readFileFunction(), "%d audio files have been added!");
        }
    }

    private void findLyrics() {
        process(core.getRecords(), core.findLyricsFunction(), "Lyrics for %d songs have been found!");
    }

    private void writeLyrics() {
        process(core.getRecordsWithLyrics(), core.writeLyricsFunction(), "Lyrics for %d songs have been written!");
    }

    private <T> void process(Collection<T> items, Function<T, Boolean> action, String message) {
        JProgressBar progress = getProgressBar(items);

        JDialog dialog = getDialog(progress);

        new Thread(() -> {
            invokeLater(() -> dialog.setVisible(true));

            AtomicInteger count = new AtomicInteger();

            items.parallelStream().forEach(item -> {
                try {
                    if (action.apply(item)) {
                        count.getAndIncrement();
                    }
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                } catch (Exception e) {
                    log.error("Action failed for item {}", item, e);
                    JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage());
                    System.exit(1);
                }
                progress.setValue(progress.getValue() + 1);
            });

            next();
            invokeLater(() -> dialog.setVisible(false));
            showMessageDialog(this, format(message, count.get()));
        }).start();
    }

    private JDialog getDialog(JProgressBar progress) {
        JDialog dialog = new JDialog(this, "Processing...", true);
        dialog.add(CENTER, progress);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 75);
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private <T> JProgressBar getProgressBar(Collection<T> items) {
        JProgressBar progress = new JProgressBar();
        progress.setMaximum(items.size());
        progress.setValue(0);
        progress.setStringPainted(true);
        progress.setIndeterminate(false);
        return progress;
    }

    private void next() {
        step = step.next();
        if (step == START || step == READ_FILES) {
            core.setRecords(new ArrayList<>());
        }
        checkBox.setEnabled(step == START || step == READ_FILES);
        invokeLater(() -> buttons.forEach((k, v) -> v.setEnabled(k == step)));
    }

    class Table extends JTable {
        private static final int COL_STATUS = 5;
        private static final int COL_LYRICS = 6;

        Table() {
            super();

            setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                    AudioFileRecord.Status status = (AudioFileRecord.Status) table.getModel().getValueAt(row, COL_STATUS);

                    switch (status) {
                        case LYRICS_FOUND:
                            setBackground(YELLOW);
                            setToolTipText((String) table.getModel().getValueAt(row, COL_LYRICS));
                            break;
                        case LYRICS_WRITTEN:
                            setBackground(GREEN);
                            break;
                        case LYRICS_NOT_FOUND:
                            setBackground(LIGHT_GRAY);
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
                    return core.getRecords().size();
                }

                public Object getValueAt(int row, int col) {
                    switch (col) {
                        case 0:
                            return row + 1;
                        case 1:
                            return core.getRecords().get(row).getArtist();
                        case 2:
                            return core.getRecords().get(row).getAlbum();
                        case 3:
                            return core.getRecords().get(row).getTitle();
                        case 4:
                            return core.getRecords().get(row).getFile().getName();
                        case 5:
                            return core.getRecords().get(row).getStatus();
                        case 6:
                            return "<html>" + core.getRecords().get(row).getLyrics().replaceAll("\n", "<br/>") + "</html>";
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

}