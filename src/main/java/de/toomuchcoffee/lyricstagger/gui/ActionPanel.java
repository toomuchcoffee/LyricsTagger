package de.toomuchcoffee.lyricstagger.gui;

import com.google.common.collect.ImmutableMap;
import de.toomuchcoffee.lyricstagger.lyrics.LyricsWikiaFinder;
import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.tagging.Tagger;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.function.Function;

import static de.toomuchcoffee.lyricstagger.gui.ActionPanel.Step.*;
import static de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord.Status.*;
import static java.util.stream.Collectors.toList;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.jaudiotagger.tag.FieldKey.LYRICS;

class ActionPanel extends JPanel {
    private Main main;
    private LyricsWikiaFinder finder = new LyricsWikiaFinder();
    private Tagger tagger = new Tagger();

    private Step step = START;

    private Map<Step, JButton> buttons = ImmutableMap.of(
            READ_FILES, new JButton("Add music library path"),
            FIND_LYRICS, new JButton("Find lyrics"),
            WRITE_LYRICS, new JButton("Write lyrics"));

    ActionPanel(Main main) {
        super();
        this.main = main;
        buttons.values().forEach(this::add);
        next();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);

        buttons.get(READ_FILES).addActionListener(e -> findAudioFiles());
        buttons.get(FIND_LYRICS).addActionListener(e -> findLyrics());
        buttons.get(WRITE_LYRICS).addActionListener(e -> writeLyrics());
    }

    void next() {
        step = step.next();
        invokeLater(() -> buttons.forEach((key, value) -> value.setEnabled(key == step)));
    }

    private void findAudioFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);

        int returnVal = fileChooser.showOpenDialog(main);
        if (returnVal == APPROVE_OPTION) {
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
            tagger.writeToFile(record.getFile(), LYRICS, record.getLyrics());
            record.setStatus(LYRICS_WRITTEN);
            return true;
        };

        main.processActionAndUpdateGUI(recordsWithLyrics, writeLyrics, "Lyrics for %d songs have been written!");
    }

    enum Step {
        START, READ_FILES, FIND_LYRICS, WRITE_LYRICS;

        public Step next() {
            switch (this) {
                case READ_FILES:
                    return FIND_LYRICS;
                case FIND_LYRICS:
                    return WRITE_LYRICS;
                case START:
                case WRITE_LYRICS:
                default:
                    return READ_FILES;
            }
        }
    }
}
