package de.toomuchcoffee.lyricstagger.lyrics;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class LyricsWikiaFinderTest {

    private LyricsWikiaFinder finder = new LyricsWikiaFinder();

    @Test
    public void happyPath() {
        String lyrics = finder.findLyrics("Queen", "Radio Ga Ga");
        assertThat(lyrics).startsWith("I'd sit alone and watch your light");
    }

    @Test
    public void identifyInstrumental() {
        String lyrics = finder.findLyrics("Deodato", "Also Sprach Zarathustra");
        assertThat(lyrics).isEqualTo("Instrumental");
    }

    @Test
    public void skipUnlicensedLyricsText() {
        String lyrics = finder.findLyrics("The Beach Boys", "Roller Skating Child");
        assertThat(lyrics).isNull();
    }

    @Test
    public void findLyricsForTitlesWhichRaiseSaxException() {
        String lyrics = finder.findLyrics("Deodato", "Also Sprach Zarathustra (2001)");
        assertThat(lyrics).isNotNull();
    }
}