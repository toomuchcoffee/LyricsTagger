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
    public void artistSimilarityTest() {
        boolean matches = lyricsFinder.matchArtistName("James Gang", "James Gang (band)");
        assertThat(matches).isTrue();
    }

    @Test
    public void songSimilarityTest() {
        Set<String> songs = newHashSet("Wonderworld");

        Set<Result> pool = newHashSet(new Result(null, "Otherworld", null));

        songs.forEach(song -> lyricsFinder.findMostSimilarSongTitle(pool, song)
                .ifPresent(t -> fail("No similarity wanted: " + song + ", but found: " + t)));

    }
}