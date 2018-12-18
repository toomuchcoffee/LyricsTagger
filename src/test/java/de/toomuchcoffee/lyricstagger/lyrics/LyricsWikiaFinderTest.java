package de.toomuchcoffee.lyricstagger.lyrics;

import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
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
    public void shouldNotCacheResult() {
        finder.findLyrics("Queen", "Radio Ga Ga");
        Optional<String> lyrics = finder.findLyrics("Queen", "I Want To Break Free");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).startsWith("I want to break free");
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

        songs.forEach(song -> finder.findMostSimilarTerm(pool, song, (int) (song.length() * 0.3))
                .ifPresent(t -> fail("No similarity wanted: " + song + ", but found: " + t)));

    }
}