package de.toomuchcoffee.lyricstagger.core.lyrics;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.inject.util.Modules.EMPTY_MODULE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;


public class LyricsFinderTest {

    private LyricsFinder lyricsFinder;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(EMPTY_MODULE);
        lyricsFinder = injector.getInstance(LyricsFinder.class);
    }

    @Test
    public void happyPath() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Queen", "Radio Ga Ga");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).startsWith("I'd sit alone and watch your light");
        } else {
            fail();
        }
    }

    @Test
    public void identifyInstrumental() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Deodato", "Also Sprach Zarathustra");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).isEqualTo("Instrumental");
        } else {
            fail();
        }
    }

    @Test
    public void skipUnlicensedLyricsText() {
        Optional<String> lyrics = lyricsFinder.findLyrics("The Beach Boys", "Roller Skating Child");
        assertThat(lyrics).isNotPresent();
    }

    @Test
    public void dontFindLyricsForNonExistingSong() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Foo", "Bar");
        assertThat(lyrics).isNotPresent();
    }

    @Test
    public void findLyricsForTitlesWhichRaiseSaxException() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Deodato", "Also Sprach Zarathustra (2001)");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsWhichShouldHaveAnAmpersand() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Steve Vai", "Here And Now");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsWithoutExclamationMark() {
        Optional<String> lyrics = lyricsFinder.findLyrics("The Who", "We're Not Gonna Take It !");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsForMedleys() {
        Optional<String> lyrics = lyricsFinder.findLyrics("The Beatles", "being for the benefit of mr. kite / i want you (she's so heavy) / helter skelter");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).containsIgnoringCase("The Hendersons");
            assertThat(lyrics.get()).containsIgnoringCase("I want you so bad");
            assertThat(lyrics.get()).containsIgnoringCase("Helter Skelter");
        } else {
            fail();
        }
    }

    @Test
    public void similarityTest() {
        Set<String> songs = newHashSet(
                "Carolina County Ball",
                "L.A. 59",
                "Ain't It All Amusing",
                "Happy",
                "Annie New Orleans",
                "Rocking Chair Rock'N'Roll Blues",
                "Rainbow",
                "Do The Same Thing",
                "Blanche",
                "Trying To Burn The Sun",
                "Black Swampy Water",
                "Prentice Wood",
                "When She Smiles",
                "Good Time Music",
                "Liberty Road",
                "Shotgun Boogie",
                "Wonderworld",
                "Streetwalker"
        );

        Set<String> pool = newHashSet("Fever Dreams",
                "Before The Fall",
                "Gypsy",
                "Breathless",
                "Stargazer",
                "King Of Rock 'N' Roll (Live)",
                "Night Music",
                "Over Love",
                "Another Lie",
                "Dream Evil",
                "Walk On Water",
                "Pain",
                "When A Woman Cries",
                "Black",
                "Bring Down The Rain",
                "Otherworld",
                "Don't Tell The Kids");

        songs.forEach(song -> lyricsFinder.findMostSimilarSongTitle(pool, song, (int) (song.length() * 0.3))
                .ifPresent(t -> fail("No similarity wanted: " + song + ", but found: " + t)));

    }
}