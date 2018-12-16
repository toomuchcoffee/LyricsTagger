package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.tagging.Tagger;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord.Status.*;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;


@Slf4j
public class Main extends JFrame {

    private JTable table;

    private Buttons buttons = new Buttons();
    private ProgressBar progress = new ProgressBar();

    private List<AudioFileRecord> records = new ArrayList<>();

    private LyricsWikiaFinder finder = new LyricsWikiaFinder();
    private Tagger tagger = new Tagger();

    public Main(String title) {
        super(title);

        JPanel p = new JPanel(new BorderLayout());

        JPanel btnsNorth = new JPanel();
        p.add(btnsNorth, BorderLayout.NORTH);

        btnsNorth.add(buttons);

        JPanel btnsSouth = new JPanel();
        progress.setStringPainted(true);
        progress.setString("Title");
        btnsSouth.add(progress);

        p.add(btnsSouth, BorderLayout.SOUTH);

        table = new JTable();
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                AudioFileRecord record = records.get(row);
                switch (record.getStatus()) {
                    case LYRICS_FOUND:
                        setBackground(Color.YELLOW);
                        setToolTipText("<html>" + record.getLyrics().replaceAll("\n", "<br/>") + "</html>");
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
        table.setModel(new DefaultTableModel() {
            private String[] columns = new String[]{"Nr", "Artist", "Album", "Title", "File"};

            public int getColumnCount() {
                return columns.length;
            }

            public int getRowCount() {
                return records.size();
            }

            public Object getValueAt(int row, int col) {
                switch (col) {
                    case 0:
                        return row + 1;
                    case 1:
                        return records.get(row).getArtist();
                    case 2:
                        return records.get(row).getAlbum();
                    case 3:
                        return records.get(row).getTitle();
                    case 4:
                        return records.get(row).getFile().getName();
                    default:
                        return null;
                }
            }

            public String getColumnName(int col) {
                return columns[col];
            }
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        getContentPane().add(p);
    }

    private void findAudioFiles(File baseDir) {
        records = new ArrayList<>();
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
            new Thread(() -> {
                buttons.disableAll();

                progress.setShowValues(false);
                progress.setIndeterminate(true);

                Collection<File> files = listFiles(baseDir, null, true);

                progress.setMaximum(files.size());
                progress.setValue(0);
                progress.setShowValues(true);
                progress.setIndeterminate(false);

                for (File file : files) {
                    tagger.readFile(file).ifPresent(records::add);
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                    progress.setValue(progress.getValue() + 1);
                }

                buttons.next();
            }).start();
        }
    }

    private void findLyrics() {
        new Thread(() -> {
            buttons.disableAll();

            progress.setMaximum(records.size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord record : records) {
                String lyrics = finder.findLyrics(record.getArtist(), record.getTitle());
                if (lyrics != null) {
                    record.setLyrics(lyrics);
                    record.setStatus(LYRICS_FOUND);
                } else {
                    record.setStatus(LYRICS_NOT_FOUND);
                }
                invokeLater(() -> {
                    table.repaint();
                    table.revalidate();
                });

                progress.setValue(progress.getValue() + 1);
            }

            buttons.next();
        }).start();
    }

    private void writeLyrics() {
        new Thread(() -> {
            buttons.disableAll();

            List<AudioFileRecord> recordsWithLyrics = records.stream().filter(r -> r.getLyrics() != null).collect(toList());

            progress.setMaximum(recordsWithLyrics.size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord record : recordsWithLyrics) {
                try {
                    tagger.writeToFile(record.getFile(), FieldKey.LYRICS, record.getLyrics());
                    record.setStatus(LYRICS_WRITTEN);
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                } catch (Exception e) {
                    log.error("Failed to write lyrics to file: {}", record.getFile().getAbsolutePath(), e);
                }
                progress.setValue(progress.getValue() + 1);
            }
            buttons.next();
        }).start();
    }

    private class Buttons extends JPanel {
        private Step step = Step.START;

        private JButton button1 = new JButton("Add music library path");
        private JButton button2 = new JButton("Find lyrics");
        private JButton button3 = new JButton("Write lyrics");

        Buttons() {
            super();
            add(button1);
            add(button2);
            add(button3);
            next();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            button1.addActionListener(e -> {
                int returnVal = fileChooser.showOpenDialog(Main.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File baseDir = fileChooser.getSelectedFile();
                    findAudioFiles(baseDir);
                }
            });
            button2.addActionListener(e -> findLyrics());
            button3.addActionListener(e -> writeLyrics());
        }

        void next() {
            disableAll();
            step = step.next();
            switch (step) {
                case FIND_LYRICS:
                    invokeLater(() -> button2.setEnabled(true));
                    break;
                case WRITE_LYRICS:
                    invokeLater(() -> button3.setEnabled(true));
                    break;
                case READ_FILES:
                default:
                    invokeLater(() -> button1.setEnabled(true));
            }
        }

        void disableAll() {
            invokeLater(() -> {
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
            });
        }

    }

}