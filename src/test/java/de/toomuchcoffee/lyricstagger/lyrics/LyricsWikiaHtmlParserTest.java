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
            assertThat(lyrics.get()).isEqualTo(
                    "The city is a ravin' neon nightmare\n" +
                    "Freudian symbols lay my soul bare\n" +
                    "And every way I turn\n" +
                    "Electric hoardings burn\n" +
                    "And words that mean nothing\n" +
                    "Are endlessly rushing\n" +
                    "Telling me nothing I really wanna learn\n" +
                    "\n" +
                    "A million lonely people in a hurry\n" +
                    "Sharin' nothin' but a burden of worry\n" +
                    "They're goin' nowhere at all\n" +
                    "They're runnin' at a crawl\n" +
                    "And of all the faces near to me\n" +
                    "Only one ever speaks to me\n" +
                    "And that's the face of the clock on the wall\n" +
                    "\n" +
                    "You've got to run so hard\n" +
                    "Just to stay where you are\n" +
                    "You've got to work\n" +
                    "You've got to earn\n" +
                    "You've got to spend\n" +
                    "The more you have, the more you want\n" +
                    "A spiral without end");
        } else {
            fail("No lyrics found. Should have found some lyrics.");
        }
    }
}