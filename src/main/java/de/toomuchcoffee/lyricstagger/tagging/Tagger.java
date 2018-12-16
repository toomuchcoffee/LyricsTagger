package de.toomuchcoffee.lyricstagger.tagging;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.Optional;

@Slf4j
public class Tagger {

    public Optional<AudioFileRecord> readFile(File file) {
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();

            String lyrics = tag.getFirst(FieldKey.LYRICS);
            if (lyrics == null || lyrics.trim().length() == 0 || lyrics.trim().equalsIgnoreCase("not found")) {
                return Optional.of(new AudioFileRecord(file, tag.getFirst(FieldKey.ARTIST), tag.getFirst(FieldKey.ALBUM), tag.getFirst(FieldKey.TITLE)));
            }
        } catch (CannotReadException e) {
            log.warn("file is not an audio file: {}", file.getAbsolutePath(), e);
        } catch (Exception e) {
            log.error("failed to read file: {}" + file.getAbsolutePath(), e);
        }
        return Optional.empty();
    }

    public void writeToFile(File file, FieldKey key, String value) {
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            tag.setField(key, value);
            f.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
