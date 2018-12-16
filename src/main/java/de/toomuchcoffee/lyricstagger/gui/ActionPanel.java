package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.tagging.Tagger;
import org.jaudiotagger.tag.FieldKey;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord.Status.*;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;

class ActionPanel extends JPanel {
    private Main main;
    private LyricsWikiaFinder finder = new LyricsWikiaFinder();
    private Tagger tagger = new Tagger();

    private Step step = Step.START;

    private JButton button1 = new JButton("Add music library path");
    private JButton button2 = new JButton("Find lyrics");
    private JButton button3 = new JButton("Write lyrics");

    ActionPanel(Main main) {
        super();
        this.main = main;
        add(button1);
        add(button2);
        add(button3);
        next();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        button1.addActionListener(e -> findAudioFiles());
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

    private void findAudioFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fileChooser.showOpenDialog(main);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            main.setRecords(new ArrayList<>());
            File baseDir = fileChooser.getSelectedFile();

            Collection<File> files = listFiles(baseDir, null, true);

            Function<File, Boolean> findFiles = file -> {
                Optional<AudioFileRecord> recordOptional = tagger.readFile(file);
                if (recordOptional.isPresent()) {
                    main.getRecords().add(recordOptional.get());
                    return true;
                }
                return false;
            };
            main.processActionAndUpdateGUI(files, findFiles, "%d audio files have been added!");
        }
    }

    private void findLyrics() {
        Function<AudioFileRecord, Boolean> findLyrics = record -> {
            String lyrics = finder.findLyrics(record.getArtist(), record.getTitle());
            if (lyrics != null) {
                record.setLyrics(lyrics);
                record.setStatus(LYRICS_FOUND);
                return true;
            } else {
                record.setStatus(LYRICS_NOT_FOUND);
                return false;
            }
        };

        main.processActionAndUpdateGUI(main.getRecords(), findLyrics, "Lyrics for %d songs have been found!");
    }

    private void writeLyrics() {
        List<AudioFileRecord> recordsWithLyrics = main.getRecords().stream()
                .filter(r -> r.getLyrics() != null)
                .collect(toList());

        Function<AudioFileRecord, Boolean> writeLyrics = record -> {
            tagger.writeToFile(record.getFile(), FieldKey.LYRICS, record.getLyrics());
            record.setStatus(LYRICS_WRITTEN);
            return true;
        };

        main.processActionAndUpdateGUI(recordsWithLyrics, writeLyrics, "Lyrics for %d songs have been written!");
    }
}
