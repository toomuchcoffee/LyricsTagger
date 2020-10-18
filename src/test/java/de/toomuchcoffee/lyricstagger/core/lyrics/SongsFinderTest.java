package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.GeniusSearchResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SongsFinderTest {

    @Mock
    private GeniusClient geniusClient;

    private SongsFinder finder;

    @Before
    public void setUp() throws Exception {
        finder = new SongsFinder(geniusClient);

        when(geniusClient.search(anyString())).thenReturn(new GeniusSearchResponse());
    }

    @Test
    public void getSongs() {
        for (int i = 0; i < 10_000; i++) {
            if (i % 10 == 0) {
                finder.getSongs("Queen");
            } else {
                finder.getSongs(i + "");
            }
        }
        verify(geniusClient).search("Queen");
    }
}