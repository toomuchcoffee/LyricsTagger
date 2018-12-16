package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.tagging.Tagger;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;


@Slf4j
public class Main extends JFrame {
    private JButton button1 = new JButton("Add music library path");
    private JButton button2 = new JButton("Find lyrics");
    private JButton button3 = new JButton("Write lyrics");

    private JTable table;

    private ProgressBar progress = new ProgressBar();

    private List<AudioFileRecord> records = new ArrayList<>();

    private LyricsWikiaFinder finder = new LyricsWikiaFinder();
    private Tagger tagger = new Tagger();

    public Main(String title) {
        super(title);

        JPanel p = new JPanel(new BorderLayout());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JPanel btnsNorth = new JPanel();
        p.add(btnsNorth, BorderLayout.NORTH);

        button1.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(Main.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File baseDir = fileChooser.getSelectedFile();
                findAudioFiles(baseDir);
            }
        });
        button2.addActionListener(e -> findLyrics());
        button3.addActionListener(e -> writeLyrics());
        btnsNorth.add(button1);
        btnsNorth.add(button2);
        btnsNorth.add(button3);

        switchButtons(Step.READ_FILES);

        JPanel btnsSouth = new JPanel();
        progress.setStringPainted(true);
        progress.setString("Title");
        btnsSouth.add(progress);

        p.add(btnsSouth, BorderLayout.SOUTH);

        table = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                AudioFileRecord record = records.get(row);
                if (record.getLyrics() != null && c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    jc.setToolTipText("<html>" + record.getLyrics().replaceAll("\n", "<br/>") + "</html>");
                }
                return c;
            }
        };
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        getContentPane().add(p);
    }

    private void findAudioFiles(File baseDir) {
        records = new ArrayList<>();
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
            new Thread(() -> {
                invokeLater(this::disableButtons);

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

                switchButtons(Step.FIND_LYRICS);
            }).start();

            table.setModel(new DefaultTableModel() {
                private String[] columns = new String[]{"Nr", "Artist", "Album", "Title", "File", "Status"};

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

    private void findLyrics() {
        new Thread(() -> {
            invokeLater(this::disableButtons);

            progress.setMaximum(records.size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord record : records) {
                String lyrics = finder.findLyrics(record.getArtist(), record.getTitle());
                if (lyrics != null) {
                    record.setLyrics(lyrics);
                    record.setStatus("LYRICS FOUND");
                } else {
                    record.setStatus("NO LYRICS FOUND");
                }
                invokeLater(() -> {
                    table.repaint();
                    table.revalidate();
                });

                progress.setValue(progress.getValue() + 1);
            }

            switchButtons(Step.WRITE_LYRICS);
        }).start();
    }

    private void writeLyrics() {
        new Thread(() -> {
            invokeLater(this::disableButtons);

            List<AudioFileRecord> recordsWithLyrics = records.stream().filter(r -> r.getLyrics() != null).collect(toList());

            progress.setMaximum(recordsWithLyrics.size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord record : recordsWithLyrics) {
                try {
                    tagger.writeToFile(record.getFile(), FieldKey.LYRICS, record.getLyrics());
                    record.setStatus("LYRICS WRITTEN");
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                } catch (Exception e) {
                    log.error("Failed to write lyrics to file: {}", record.getFile().getAbsolutePath(), e);
                }
                progress.setValue(progress.getValue() + 1);
            }
            switchButtons(Step.READ_FILES);
        }).start();
    }

    private void switchButtons(Step step) {
        switch (step) {
            case READ_FILES:
                invokeLater(() -> {
                    disableButtons();
                    button1.setEnabled(true);
                });
                break;
            case FIND_LYRICS:
                invokeLater(() -> {
                    disableButtons();
                    button2.setEnabled(true);
                });
                break;
            case WRITE_LYRICS:
                invokeLater(() -> {
                    disableButtons();
                    button3.setEnabled(true);
                });
                break;
            default:
                invokeLater(this::disableButtons);
        }
    }

    private void disableButtons() {
        button1.setEnabled(false);
        button2.setEnabled(false);
        button3.setEnabled(false);
    }

    private enum Step {
        READ_FILES, FIND_LYRICS, WRITE_LYRICS
    }

}