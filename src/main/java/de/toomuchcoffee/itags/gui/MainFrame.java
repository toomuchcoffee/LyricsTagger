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
    private JButton button = new JButton("Add music library path");
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

        button.addActionListener(this);
        button.setActionCommand(ACTION_COMMAND_FIND_FILES);
        btnsNorth.add(button);

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
        JFrame frame = new MainFrame("iTags: add lyrics to your music library");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void findAudioFiles() {
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
            tagger = new Tagger();

            new Thread(() -> {
                running = true;

                invokeLater(() -> button.setEnabled(false));

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

                changeButton(false);
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
        new Thread(() -> {
            running = true;

            invokeLater(() -> button.setEnabled(false));

            progress.setMaximum(tagger.getRecords().size());
            progress.setValue(0);
            progress.setShowValues(true);
            progress.setIndeterminate(false);

            for (AudioFileRecord aRecord : tagger.getRecords()) {
                if (!running)
                    break;
                String lyrics = LyricsWikiaFinder.findLyrics(aRecord.getArtist(), aRecord.getTitle());
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
                changeButton(false);
        }).start();
    }

    private void writeLyrics() {
        new Thread(() -> {
            running = true;

            invokeLater(() -> button.setEnabled(false));

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
                changeButton(false);
        }).start();
    }

    private void changeButton(boolean reset) {
        if (reset) {
            invokeLater(() -> {
                button.setActionCommand(ACTION_COMMAND_FIND_FILES);
                button.setText("Add music library path");
                button.setEnabled(true);
            });
        } else if (ACTION_COMMAND_FIND_FILES.equals(button.getActionCommand())) {
            invokeLater(() -> {
                button.setActionCommand(ACTION_COMMAND_FIND_LYRICS);
                button.setText("Find lyrics");
                button.setEnabled(true);
            });
        } else if (ACTION_COMMAND_FIND_LYRICS.equals(button.getActionCommand())) {
            invokeLater(() -> {
                button.setActionCommand(ACTION_COMMAND_WRITE_LYRICS);
                button.setText("Write lyrics");
                button.setEnabled(true);
            });
        } else if (ACTION_COMMAND_WRITE_LYRICS.equals(button.getActionCommand())) {
            invokeLater(() -> button.setEnabled(false));
        }
    }

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
            changeButton(true);
        }
    }

}