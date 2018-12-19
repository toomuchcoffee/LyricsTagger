package de.toomuchcoffee.lyricstagger.core.record;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;

import static de.toomuchcoffee.lyricstagger.core.record.AudioFileRecord.Status.INITIAL;

@Getter
@Setter
@RequiredArgsConstructor
public class AudioFileRecord {
    private final File file;
    private final String artist;
    private final String album;
    private final String title;
    private String lyrics;
    private Status status = INITIAL;

    public enum Status {
        INITIAL, LYRICS_NOT_FOUND, LYRICS_FOUND, LYRICS_WRITTEN
    }
}
