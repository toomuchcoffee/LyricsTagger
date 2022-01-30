package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.gui.MainFrame;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class GeniusClientTest {

    @MockBean
    private MainFrame mainFrame;

    @Value("${genius.api-client.access-token}")
    private String accessToken;

    @Autowired
    private GeniusClient geniusClient;

    @Test
    void findLyrics() {
        System.out.println(accessToken);
        GeniusClient.SongResult songResponse = geniusClient.song( 309595L, "Bearer " + accessToken);
        assertThat(songResponse.getResponse().getSong().getUrl())
                .isEqualTo("https://genius.com/Queen-radio-ga-ga-lyrics");
    }

    @Test
    void findSongs() {
        GeniusClient.SearchResult searchResponse = geniusClient.search("ABC", "Bearer " + accessToken);
        assertThat(searchResponse.getResponse().getHits().size()).isGreaterThan(5);
        //assertThat(searchResponse).contains("Radio Ga Ga");
    }
}