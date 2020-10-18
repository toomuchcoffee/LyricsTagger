package de.toomuchcoffee.lyricstagger.core;

import com.google.inject.Inject;
import de.toomuchcoffee.lyricstagger.core.lyrics.LyricsFinder;
import de.toomuchcoffee.lyricstagger.core.record.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.core.record.Reader;
import de.toomuchcoffee.lyricstagger.core.record.Writer;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.toomuchcoffee.lyricstagger.core.record.AudioFileRecord.Status.*;
import static java.util.stream.Collectors.toList;

public class CoreFacade {
    private final Reader reader;
    private final LyricsFinder lyricsFinder;
    private final Writer writer;

    @Getter
    @Setter
    private boolean findAll;

    @Getter
    @Setter
    private List<AudioFileRecord> records;

    @Inject
    public CoreFacade(LyricsFinder lyricsFinder, Reader reader, Writer writer) {
        this.lyricsFinder = lyricsFinder;
        this.reader = reader;
        this.writer = writer;
    }

    public Function<File, Boolean> readFileFunction() {
        return file -> {
            Optional<AudioFileRecord> recordOptional = reader.readFile(file, findAll);
            if (recordOptional.isPresent()) {
                records.add(recordOptional.get());
                return true;
            }
            return false;
        };
    }

    public Function<AudioFileRecord, Boolean> findLyricsFunction() {
        return record -> {
            Optional<String> lyrics = lyricsFinder.findLyrics(record.getTitle(), record.getArtist());
            if (lyrics.isPresent()) {
                record.setLyrics(lyrics.get());
                record.setStatus(LYRICS_FOUND);
                return true;
            } else {
                record.setStatus(LYRICS_NOT_FOUND);
                return false;
            }
        };
    }

    public Function<AudioFileRecord, Boolean> writeLyricsFunction() {
        return record -> {
            writer.writeToFile(record.getFile(), record.getLyrics());
            record.setStatus(LYRICS_WRITTEN);
            return true;
        };
    }

    public List<AudioFileRecord> getRecordsWithLyrics() {
        return records.stream()
                .filter(r -> r.getLyrics() != null)
                .collect(toList());
    }
}
