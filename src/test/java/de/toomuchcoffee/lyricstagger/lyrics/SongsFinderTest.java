package de.toomuchcoffee.lyricstagger.lyrics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SongsFinderTest {

    @Mock
    private LyricsWikiaJsonParser jsonParser;

    private SongsFinder finder;

    @Before
    public void setUp() throws Exception {
        finder = new SongsFinder(jsonParser);

        when(jsonParser.findSongs(anyString())).thenReturn(newHashSet(randomUUID().toString()));
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
        verify(jsonParser).findSongs("Queen");
    }
}