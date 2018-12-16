package de.toomuchcoffee.itags.gui;

import de.toomuchcoffee.itags.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.itags.tagging.AudioFileRecord;
import de.toomuchcoffee.itags.tagging.Tagger;
import org.jaudiotagger.tag.FieldKey;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;

import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;


public class MainFrame extends JFrame implements ActionListener {
    private static final String ACTION_COMMAND_FIND_FILES = "findFiles";
    private static final String ACTION_COMMAND_FIND_LYRICS = "findLyrics";
    private static final String ACTION_COMMAND_WRITE_LYRICS = "writeLyrics";
    private static final String ACTION_COMMAND_CANCEL = "cancel";

    private JFileChooser fileChooser;

    private JButton button1 = new JButton("Add music library path");
    private JButton button2 = new JButton("Add music library path");
    private JButton button3 = new JButton("Add music library path");

    private JTable table;

    private ProgressBar progress = new ProgressBar();

    private File baseDir;
    private String[] columns = new String[]{"Nr", "Artist", "Album", "Title", "File", "Status"};
    private Tagger tagger;

    private boolean running;

    public MainFrame(String title) {
        super(title);

        JPanel p = new JPanel(new BorderLayout());

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JPanel btnsNorth = new JPanel();
        p.add(btnsNorth, BorderLayout.NORTH);

        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
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
        cancelBtn.addActionListener(this);
        cancelBtn.setActionCommand(ACTION_COMMAND_CANCEL);
        btnsSouth.add(cancelBtn);

        table = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                AudioFileRecord record = tagger.getRecords().get(row);
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

                invokeLater(() -> enableButtons(false));

                progress.setShowValues(false);
                progress.setIndeterminate(true);

                Collection<File> allFiles = listFiles(baseDir, null, true);

                progress.setMaximum(allFiles.size());
                progress.setValue(0);
                progress.setShowValues(true);
                progress.setIndeterminate(false);

                for (File aFile : allFiles) {
                    if (!running)
                        break;
                    tagger.readFile(aFile);
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
                    return tagger.getRecords().size();
                }

                public Object getValueAt(int row, int col) {
                    if (col == 0) // row number
                        return row + 1;
                    else { // record value
                        AudioFileRecord record = tagger.getRecords().get(row);
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

            invokeLater(() -> enableButtons(false));

            progress.setMaximum(tagger.getRecords().size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord aRecord : tagger.getRecords()) {
                if (!running)
                    break;
                String lyrics = finder.findLyrics(aRecord.getArtist(), aRecord.getTitle());
                if (lyrics != null) {
                    aRecord.setLyrics(lyrics);
                    aRecord.setStatus("LYRICS FOUND");
                } else {
                    aRecord.setStatus("NO LYRICS FOUND");
                }
                invokeLater(() -> {
                    table.repaint();
                    table.revalidate();
                });

                progress.setValue(progress.getValue() + 1);
            }

            if (running)
                switchButtons(Step.WRITE_LYRICS);
        }).start();
    }

    private void writeLyrics() {
        new Thread(() -> {
            running = true;

            invokeLater(() -> enableButtons(false));

            progress.setMaximum(tagger.getRecords().size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord aRecord : tagger.getRecords()) {
                if (!running)
                    break;
                try {
                    if (aRecord.getLyrics() != null) {
                        tagger.writeToFile(aRecord.getFile(), FieldKey.LYRICS, aRecord.getLyrics());
                        aRecord.setStatus("LYRICS WRITTEN");
                        invokeLater(new Runnable() {
                            public void run() {
                                table.repaint();
                                table.revalidate();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progress.setValue(progress.getValue() + 1);
            }
            if (running)
                switchButtons(Step.READ_FILES);
        }).start();
    }

    private void switchButtons(Step step) {
        switch (step) {
            case READ_FILES:
                invokeLater(() -> {
                    button1.setActionCommand(ACTION_COMMAND_FIND_FILES);
                    button1.setText("Add music library path");
                    enableButtons(false);
                    button1.setEnabled(true);
                });
                break;
            case FIND_LYRICS:
                invokeLater(() -> {
                    button2.setActionCommand(ACTION_COMMAND_FIND_LYRICS);
                    button2.setText("Find lyrics");
                    enableButtons(false);
                    button2.setEnabled(true);
                });
                break;
            case WRITE_LYRICS:
                invokeLater(() -> {
                    button3.setActionCommand(ACTION_COMMAND_WRITE_LYRICS);
                    button3.setText("Write lyrics");
                    enableButtons(false);
                    button3.setEnabled(true);
                });
                break;
            default:
                invokeLater(() -> enableButtons(false));
        }
    }

    private void enableButtons(boolean enable) {
        button1.setEnabled(enable);
        button2.setEnabled(enable);
        button3.setEnabled(enable);
    }

    private enum Step {
        READ_FILES, FIND_LYRICS, WRITE_LYRICS;

        Step next() {
            switch (this) {
                case READ_FILES:
                    return FIND_LYRICS;
                case FIND_LYRICS:
                    return WRITE_LYRICS;
                case WRITE_LYRICS:
                default:
                    return READ_FILES;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (ACTION_COMMAND_FIND_FILES.equals(e.getActionCommand())) {
            int returnVal = fileChooser.showOpenDialog(MainFrame.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                baseDir = fileChooser.getSelectedFile();
                findAudioFiles();
            }
        } else if (ACTION_COMMAND_FIND_LYRICS.equals(e.getActionCommand())) {
            findLyrics();
        } else if (ACTION_COMMAND_WRITE_LYRICS.equals(e.getActionCommand())) {
            writeLyrics();
        } else if (ACTION_COMMAND_CANCEL.equals(e.getActionCommand())) {
            running = false;
            switchButtons(Step.READ_FILES);
        }
    }

}