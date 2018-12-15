package de.toomuchcoffee.itags.tagging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@RequiredArgsConstructor
public class AudioFileRecord {
    private final File file;
    private final String artist;
    private final String album;
    private final String title;
    private String lyrics;
    private String status;
}
