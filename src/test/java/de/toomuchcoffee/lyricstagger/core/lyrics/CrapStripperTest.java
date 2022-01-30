package de.toomuchcoffee.lyricstagger.core.lyrics;

import org.junit.jupiter.api.Test;

import static de.toomuchcoffee.lyricstagger.core.lyrics.CrapStripper.CRAP_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class CrapStripperTest {
    private final CrapStripper crapStripper = new CrapStripper();

    @Test
    void stripsInputFromCrap() {
        String result = crapStripper.stripFromCrap("Foo bar baz" + CRAP_STRING);

        assertThat(result).isEqualTo("Foo bar baz");
    }

    @Test
    void passesInputThroughIfNoCrap() {
        String result = crapStripper.stripFromCrap("Foo bar baz");

        assertThat(result).isEqualTo("Foo bar baz");
    }
}