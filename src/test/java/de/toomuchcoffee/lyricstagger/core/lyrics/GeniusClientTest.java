package de.toomuchcoffee.lyricstagger.core.lyrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class GeniusClientTest {

    private GeniusClient geniusClient = new GeniusClient();

    @Test
    public void findLyrics() {
        GeniusClient.GeniusSongResponse songResponse = geniusClient.song( 309595L);
        assertThat(songResponse.getResponse().getSong().getUrl())
                .isEqualTo("https://genius.com/Queen-radio-ga-ga-lyrics");
    }

    @Test
    public void findSongs() {
        GeniusClient.GeniusSearchResponse searchResponse = geniusClient.search("ABC");
        assertThat(searchResponse.getResponse().getHits().size()).isGreaterThan(5);
        //assertThat(searchResponse).contains("Radio Ga Ga");
    }
}