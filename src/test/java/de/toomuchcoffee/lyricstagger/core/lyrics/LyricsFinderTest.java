package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.Result;
import de.toomuchcoffee.lyricstagger.gui.MainFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.Set;

import static de.toomuchcoffee.lyricstagger.core.lyrics.CrapStripper.CRAP_STRING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class LyricsFinderTest {
    @MockBean
    private MainFrame mainFrame;

    @Autowired
    private LyricsFinder lyricsFinder;

    @MockBean
    private CrapStripper crapStripper;

    @BeforeEach
    void setUp() {
        when(crapStripper.stripFromCrap(anyString())).thenCallRealMethod();
    }

    @Test
    void happyPath() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Radio Ga Ga", "Queen");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).contains("I'd sit alone and watch your light \n");
        } else {
            fail();
        }
    }

    @Test
    void identifyInstrumental() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Tramontane", "Foreigner");
        if (lyrics.isPresent()) {
            assertThat(lyrics.get()).containsIgnoringCase("Instrumental");
        } else {
            fail();
        }
    }

    @Test
    void doNotFindLyricsForNonExistingSong() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Foo", "baz");
        assertThat(lyrics).isNotPresent();
    }

    @Test
    void findLyricsWhichShouldHaveAnAmpersand() {
        Optional<String> lyrics = lyricsFinder.findLyrics("Here And Now", "Steve Vai");
        assertThat(lyrics).isPresent();
    }

    @Test
    void findLyricsWithoutExclamationMark() {
        Optional<String> lyrics = lyricsFinder.findLyrics("We're Not Gonna Take It !", "The Who");
        assertThat(lyrics).isPresent();
    }

    @Test
    void artistSimilarityTest() {
        boolean matches = lyricsFinder.matchArtistName("James Gang", "James Gang (band)");
        assertThat(matches).isTrue();
    }

    @Test
    void songSimilarityTest() {
        Set<String> songs = Set.of("Wonderworld");

        Set<Result> pool = Set.of(new Result(null, "Otherworld", null));

        songs.forEach(song -> lyricsFinder.findMostSimilarSongTitle(pool, song)
                .ifPresent(t -> fail("No similarity wanted: " + song + ", but found: " + t)));

    }

    @Test
    void usesCrapStripper() {
        lyricsFinder.findLyrics("Post War Saturday Echo", "Quatermass");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(crapStripper).stripFromCrap(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).contains(CRAP_STRING);
    }
}