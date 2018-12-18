package de.toomuchcoffee.lyricstagger.lyrics;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class LyricsWikiaHtmlParserTest {

    private LyricsWikiaHtmlParser parser = new LyricsWikiaHtmlParser();

    @Test
    public void findLyricsForInstrumentalShouldOnlyContainText() {
        Optional<String> lyrics = parser.findLyrics(
                "http://lyrics.wikia.com/wiki/Rainbow:Vielleicht_Das_N%C3%A4chste_Mal_(Maybe_Next_Time)");

        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).isEqualTo("Instrumental");
        } else {
            fail("No lyrics found. Should have found some lyrics.");
        }
    }

    @Test
    public void findLyricsShouldOnlyContainTextWithLinebreaks() {
        Optional<String> lyrics = parser.findLyrics(
                "http://lyrics.wikia.com/wiki/Quatermass:Post_War,_Saturday_Echo");

        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).startsWith(
                    "The city is a ravin' neon nightmare\n Freudian symbols lay my soul bare\n");
        } else {
            fail("No lyrics found. Should have found some lyrics.");
        }
    }
}