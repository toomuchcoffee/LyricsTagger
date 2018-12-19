package de.toomuchcoffee.lyricstagger.core.lyrics;

import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class LyricsWikiaJsonParserTest {

    private LyricsWikiaJsonParser parser = new LyricsWikiaJsonParser();

    @Test
    public void findLyrics() {
        Optional<String> lyrics = parser.findLyrics("Queen", "Radio Gaga");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).isEqualTo("http://lyrics.wikia.com/Queen:Radio_Ga_Ga");
        } else {
            fail();
        }
    }

    @Test
    public void findSongs() {
        Set<String> songs = parser.findSongs("Queen");
        assertThat(songs.size()).isGreaterThan(50);
        assertThat(songs).contains("Radio Ga Ga");
    }
}