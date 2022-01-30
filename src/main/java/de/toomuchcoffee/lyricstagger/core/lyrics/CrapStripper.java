package de.toomuchcoffee.lyricstagger.core.lyrics;

import org.springframework.stereotype.Component;

@Component
public class CrapStripper {
    public static final String CRAP_STRING = "Embed Cancel How to Format Lyrics: ";

    public String stripFromCrap(String input) {
        int crapIndex = input.indexOf(CRAP_STRING);
        if (crapIndex < 0) {
            return input;
        }
        return input.substring(0, crapIndex).trim();
    }
}
