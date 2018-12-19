package de.toomuchcoffee.lyricstagger.core.record;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.SupportedFileFormat;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jaudiotagger.tag.FieldKey.*;

@Slf4j
public class Reader {

    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = Arrays.stream(SupportedFileFormat.values())
            .map(SupportedFileFormat::getFilesuffix)
            .collect(toSet());

    public Optional<AudioFileRecord> readFile(File file, boolean findAll) {
        String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (!SUPPORTED_FILE_EXTENSIONS.contains(fileExtension)) {
            return Optional.empty();
        }

        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();

            String lyrics = tag.getFirst(LYRICS);
            if (findAll || (lyrics == null || lyrics.trim().length() == 0)) {
                return Optional.of(new AudioFileRecord(file, tag.getFirst(ARTIST), tag.getFirst(ALBUM), tag.getFirst(TITLE)));
            }
        } catch (CannotReadException e) {
            log.warn("file is not an audio file: {}", file.getAbsolutePath(), e);
        } catch (Exception e) {
            log.error("failed to read file: {}", file.getAbsolutePath(), e);
        }
        return Optional.empty();
    }
}
