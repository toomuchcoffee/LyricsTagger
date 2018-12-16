package de.toomuchcoffee.itags.gui;

import de.toomuchcoffee.itags.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.itags.tagging.AudioFileRecord;
import de.toomuchcoffee.itags.tagging.Tagger;
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
public class MainFrame extends JFrame {
    private JFileChooser fileChooser;

    private JButton button1 = new JButton("Add music library path");
    private JButton button2 = new JButton("Find lyrics");
    private JButton button3 = new JButton("Write lyrics");

    private JTable table;

    private ProgressBar progress = new ProgressBar();

    private File baseDir;
    private String[] columns = new String[]{"Nr", "Artist", "Album", "Title", "File", "Status"};
    private Tagger tagger;

    private boolean running;

    private List<AudioFileRecord> records = new ArrayList<>();

    private MainFrame(String title) {
        super(title);

        JPanel p = new JPanel(new BorderLayout());

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JPanel btnsNorth = new JPanel();
        p.add(btnsNorth, BorderLayout.NORTH);

        button1.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(MainFrame.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                baseDir = fileChooser.getSelectedFile();
                findAudioFiles();
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

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            running = false;
            switchButtons(Step.READ_FILES);
        });
        btnsSouth.add(cancelBtn);

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

    public static void main(String[] args) {
        JFrame frame = new MainFrame("Add lyrics to your music library");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void findAudioFiles() {
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
            tagger = new Tagger();

            new Thread(() -> {
                running = true;

                invokeLater(this::disableButtons);

                progress.setShowValues(false);
                progress.setIndeterminate(true);

                Collection<File> allFiles = listFiles(baseDir, null, true);

                progress.setMaximum(allFiles.size());
                progress.setValue(0);
                progress.setShowValues(true);
                progress.setIndeterminate(false);

                for (File file : allFiles) {
                    if (!running) {
                        break;
                    }
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
                public int getColumnCount() {
                    return columns.length;
                }

                public int getRowCount() {
                    return records.size();
                }

                public Object getValueAt(int row, int col) {
                    if (col == 0) // row number
                        return row + 1;
                    else { // record value
                        AudioFileRecord record = records.get(row);
                        if (col == 1)
                            return record.getArtist();
                        if (col == 2)
                            return record.getAlbum();
                        if (col == 3)
                            return record.getTitle();
                        if (col == 4)
                            return record.getFile().getName();
                        if (col == 5)
                            return record.getStatus();
                    }
                    return null;
                }

                public String getColumnName(int col) {
                    return columns[col];
                }
            });
        }
    }

    private void findLyrics() {
        LyricsWikiaFinder finder = new LyricsWikiaFinder();
        new Thread(() -> {
            running = true;

            invokeLater(this::disableButtons);

            progress.setMaximum(records.size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord record : records) {
                if (!running)
                    break;
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

            if (running) {
                switchButtons(Step.WRITE_LYRICS);
            }
        }).start();
    }

    private void writeLyrics() {
        new Thread(() -> {
            running = true;

            invokeLater(this::disableButtons);

            List<AudioFileRecord> recordsWithLyrics = records.stream().filter(r -> r.getLyrics() != null).collect(toList());

            progress.setMaximum(recordsWithLyrics.size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord record : recordsWithLyrics) {
                if (!running)
                    break;
                try {
                    tagger.writeToFile(record.getFile(), FieldKey.LYRICS, record.getLyrics());
                    record.setStatus("LYRICS WRITTEN");
                    invokeLater(new Runnable() {
                        public void run() {
                            table.repaint();
                            table.revalidate();
                        }
                    });
                } catch (Exception e) {
                    log.error("Failed to write lyrics to file: {}", record.getFile().getAbsolutePath(), e);
                }
                progress.setValue(progress.getValue() + 1);
            }
            if (running) {
                switchButtons(Step.READ_FILES);
            }
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