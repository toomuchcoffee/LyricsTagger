package de.toomuchcoffee.lyricstagger.core.lyrics;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class GeniusLyricsScraperTest {

    private final GeniusLyricsScraper parser = new GeniusLyricsScraper();

    @Test
    public void findLyricsForInstrumentalShouldOnlyContainText() {
        Optional<String> lyrics = parser.findLyrics(
                "https://genius.com/Rainbow-vielleicht-das-nachste-mal-lyrics");

        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).containsIgnoringCase("Instrumental");
        } else {
            fail("No lyrics found. Should have found some lyrics.");
        }
    }

    @Test
    public void findLyricsShouldOnlyContainTextWithLinebreaks() {
        Optional<String> lyrics = parser.findLyrics(
                "https://genius.com/Quatermass-post-war-saturday-echo-lyrics");

        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).contains("Freudian symbols lay my soul bare \n");
        } else {
            fail("No lyrics found. Should have found some lyrics.");
        }
    }

    @Test
    public void findLyrics() {
        Optional<String> lyrics = parser.findLyrics(
                "https://genius.com/Quatermass-post-war-saturday-echo-lyrics");

        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).contains("Freudian symbols lay my soul bare \n");
        } else {
            fail("No lyrics found. Should have found some lyrics.");
        }
    }

}