package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord.Status;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord.Status.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;


@Slf4j
public class Main extends JFrame {

    private static final int COL_STATUS = 5;
    private static final int COL_LYRICS = 6;
    private JTable table;

    private Buttons buttons = new Buttons();
    private JProgressBar progress = new JProgressBar();

    private List<AudioFileRecord> records = new ArrayList<>();

    private LyricsWikiaFinder finder = new LyricsWikiaFinder();
    private Tagger tagger = new Tagger();

    public Main(String title) {
        super(title);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel northPanel = new JPanel();
        panel.add(northPanel, BorderLayout.NORTH);

        northPanel.add(buttons);

        JPanel southPanel = new JPanel();
        progress.setStringPainted(true);
        progress.setIndeterminate(false);
        southPanel.add(progress);

        panel.add(southPanel, BorderLayout.SOUTH);

        table = new JTable();
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                Status status = (Status) table.getModel().getValueAt(row, COL_STATUS);

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
                    case 5:
                        return records.get(row).getStatus();
                    case 6:
                        return "<html>" + records.get(row).getLyrics().replaceAll("\n", "<br/>") + "</html>";
                    default:
                        return null;
                }
            }

            public String getColumnName(int col) {
                return columns[col];
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        getContentPane().add(panel);
    }

    private void findAudioFiles(File baseDir) {
        records = new ArrayList<>();
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
            new Thread(() -> {
                buttons.disableAll();

                Collection<File> files = listFiles(baseDir, null, true);

                progress.setMaximum(files.size());
                progress.setValue(0);

                AtomicInteger count = new AtomicInteger();

                files.parallelStream().forEach(file -> {
                    Optional<AudioFileRecord> recordOptional = tagger.readFile(file);
                    if (recordOptional.isPresent()) {
                        records.add(recordOptional.get());
                        count.getAndIncrement();
                    }
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                    progress.setValue(progress.getValue() + 1);
                });

                buttons.next();
                JOptionPane.showMessageDialog(this, format("%d audio files have been added!", count.get()));
            }).start();
        }
    }

    private void findLyrics() {
        new Thread(() -> {
            buttons.disableAll();

            progress.setMaximum(records.size());
            progress.setValue(0);

            AtomicInteger count = new AtomicInteger();

            records.parallelStream().forEach(record -> {
                String lyrics = finder.findLyrics(record.getArtist(), record.getTitle());
                if (lyrics != null) {
                    record.setLyrics(lyrics);
                    record.setStatus(LYRICS_FOUND);
                    count.getAndIncrement();
                } else {
                    record.setStatus(LYRICS_NOT_FOUND);
                }
                invokeLater(() -> {
                    table.repaint();
                    table.revalidate();
                });

                progress.setValue(progress.getValue() + 1);
            });

            buttons.next();
            JOptionPane.showMessageDialog(this, format("Lyrics for %d songs have been found!", count.get()));
        }).start();
    }

    private void writeLyrics() {
        new Thread(() -> {
            buttons.disableAll();

            List<AudioFileRecord> recordsWithLyrics = records.stream().filter(r -> r.getLyrics() != null).collect(toList());

            progress.setMaximum(recordsWithLyrics.size());
            progress.setValue(0);

            AtomicInteger count = new AtomicInteger();

            recordsWithLyrics.parallelStream().forEach(record -> {
                try {
                    tagger.writeToFile(record.getFile(), FieldKey.LYRICS, record.getLyrics());
                    record.setStatus(LYRICS_WRITTEN);
                    count.getAndIncrement();
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                } catch (Exception e) {
                    log.error("Failed to write lyrics to file: {}", record.getFile().getAbsolutePath(), e);
                }
                progress.setValue(progress.getValue() + 1);
            });

            buttons.next();
            JOptionPane.showMessageDialog(this, format("Lyrics for %d songs have been written!", count.get()));
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