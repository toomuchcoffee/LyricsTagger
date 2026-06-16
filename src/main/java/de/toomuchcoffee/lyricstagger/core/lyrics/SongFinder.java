package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.SearchResult;
import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.SongResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Slf4j
@Component
public class SongFinder {
    private final GeniusClient geniusClient;
    private final String accessToken;

    public SongFinder(GeniusClient geniusClient, @Value("${genius.api-client.access-token}") String accessToken) {
        this.geniusClient = geniusClient;
        this.accessToken = accessToken;
    }

    @Cacheable("search")
    Optional<SearchResult> search(String query) {
        log.info("Search by: {}", query);
        try {
            SearchResult searchResult = geniusClient.search(query, getAuthorization());
            return Optional.of(searchResult);
        } catch (RestClientResponseException e) {
            log.error("Failed to execute search by: {}", query, e);
            return Optional.empty();
        }
    }

    @Cacheable("song")
    public Optional<SongResult> song(Long id) {
        log.info("Retrieving song with id: {}", id);
        try {
            SongResult songResult = geniusClient.song(id, getAuthorization());
            return Optional.of(songResult);
        } catch (RestClientResponseException e) {
            log.error("Failed to retrieve song with id: {}", id, e);
            return Optional.empty();
        }
    }

    private String getAuthorization() {
        return "Bearer " + accessToken;
    }
}
