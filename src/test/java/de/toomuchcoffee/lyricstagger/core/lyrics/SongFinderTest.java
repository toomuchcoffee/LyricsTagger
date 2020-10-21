package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.SongResult;
import de.toomuchcoffee.lyricstagger.gui.MainFrame;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SongFinderTest {

    @Autowired
    private SongFinder finder;

    @Autowired
    private GeniusClient geniusClient;

    @MockBean
    private MainFrame mainFrame;

    private final SongResult songResult = new SongResult();

    @Before
    public void setUp() throws Exception {
        when(geniusClient.song(eq(1L), eq("Bearer foo")))
                .thenReturn(songResult)
                .thenThrow(new RuntimeException("Should use cache"));
    }

    @Test
    public void shouldUseCache() {
        assertThat(finder.song(1L)).isEqualTo(Optional.of(songResult));
        assertThat(finder.song(1L)).isEqualTo(Optional.of(songResult));

        verify(geniusClient, times(1)).song(eq(1L), eq("Bearer foo"));
        verifyNoMoreInteractions(geniusClient);
    }

    @EnableCaching
    @Configuration
    public static class TestConfig {

        @Bean
        public GeniusClient geniusClient() {
            return mock(GeniusClient.class);
        }

        @Bean
        public SongFinder songsFinder(GeniusClient geniusClient) {
            return new SongFinder(geniusClient, "foo");
        }

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("song");
        }

    }

}