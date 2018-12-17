package de.toomuchcoffee.lyricstagger.lyrics;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;


public class LyricsWikiaFinderTest {

    private LyricsWikiaFinder finder = new LyricsWikiaFinder();

    @Test
    public void happyPath() {
        Optional<String> lyrics = finder.findLyrics("Queen", "Radio Ga Ga");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).startsWith("I'd sit alone and watch your light");
        } else {
            fail();
        }
    }

    @Test
    public void identifyInstrumental() {
        Optional<String> lyrics = finder.findLyrics("Deodato", "Also Sprach Zarathustra");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).isEqualTo("Instrumental");
        } else {
            fail();
        }
    }

    @Test
    public void skipUnlicensedLyricsText() {
        Optional<String> lyrics = finder.findLyrics("The Beach Boys", "Roller Skating Child");
        assertThat(lyrics).isNotPresent();
    }

    @Test
    public void dontFindLyricsForNonExistingSong() {
        Optional<String> lyrics = finder.findLyrics("Foo", "Bar");
        assertThat(lyrics).isNotPresent();
    }

    @Test
    public void findLyricsForTitlesWhichRaiseSaxException() {
        Optional<String> lyrics = finder.findLyrics("Deodato", "Also Sprach Zarathustra (2001)");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsWhichShouldHaveAnAmpersand() {
        Optional<String> lyrics = finder.findLyrics("Steve Vai", "Here And Now");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsWithoutExclamationMark() {
        Optional<String> lyrics = finder.findLyrics("The Who", "We're Not Gonna Take It !");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsForMedleys() {
        Optional<String> lyrics = finder.findLyrics("The Beatles", "being for the benefit of mr. kite / i want you (she's so heavy) / helter skelter");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).containsIgnoringCase("The Hendersons");
            assertThat(lyrics.get()).containsIgnoringCase("I want you so bad");
            assertThat(lyrics.get()).containsIgnoringCase("Helter Skelter");
        } else {
            fail();
        }
    }
}