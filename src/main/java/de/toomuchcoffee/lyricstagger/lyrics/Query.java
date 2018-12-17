package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class Query {
    private final String artist;
    private final String song;
}
