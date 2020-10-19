package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.Result;
import de.toomuchcoffee.lyricstagger.gui.MainFrame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LyricsFinderTest {
    @MockBean
    private MainFrame mainFrame;

    @Autowired
    private LyricsFinder lyricsFinder;

    @Test
    public void happyPath() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Radio Ga Ga", "Queen");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).contains("I'd sit alone and watch your light \n");
        } else {
            fail();
        }
    }

    @Test
    public void identifyInstrumental() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Tramontane", "Foreigner");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).containsIgnoringCase("Instrumental");
        } else {
            fail();
        }
    }

    @Test
    public void dontFindLyricsForNonExistingSong() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Foo", "baz");
        assertThat(lyrics).isNotPresent();
    }

    @Test
    public void findLyricsWhichShouldHaveAnAmpersand() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Here And Now", "Steve Vai");
        assertThat(lyrics).isPresent();
    }

    @Test
    public void findLyricsWithoutExclamationMark() {
        Optional<String> lyrics = lyricsFinder.findLyrics("We're Not Gonna Take It !", "The Who");
        assertThat(lyrics).isPresent();
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

        Set<Result> pool = newHashSet(
                new Result(null, "Fever Dreams", null),
                new Result(null, "Before The Fall", null),
                new Result(null, "Gypsy", null),
                new Result(null, "Breathless", null),
                new Result(null, "Stargazer", null),
                new Result(null, "King Of Rock 'N' Roll (Live)", null),
                new Result(null, "Night Music", null),
                new Result(null, "Over Love", null),
                new Result(null, "Another Lie", null),
                new Result(null, "Dream Evil", null),
                new Result(null, "Walk On Water", null),
                new Result(null, "Pain", null),
                new Result(null, "When A Woman Cries", null),
                new Result(null, "Black", null),
                new Result(null, "Bring Down The Rain", null),
                new Result(null, "Otherworld", null),
                new Result(null, "Don't Tell The Kids", null));

        songs.forEach(song -> lyricsFinder.findMostSimilarSongTitle(pool, song, (int) (song.length() * 0.3))
                .ifPresent(t -> fail("No similarity wanted: " + song + ", but found: " + t)));

    }
}