package de.toomuchcoffee.lyricstagger.records;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import java.io.File;

import static org.jaudiotagger.tag.FieldKey.LYRICS;

@Slf4j
public class Writer {

    public void writeToFile(File file, String value) {
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            tag.setField(LYRICS, value);
            f.commit();
        } catch (Exception e) {
            log.error("Failed to write to file: {}", file.getAbsolutePath(), e);
        }
    }

}
