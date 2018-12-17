package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Query {
    private final String artist;
    private final String song;
}
