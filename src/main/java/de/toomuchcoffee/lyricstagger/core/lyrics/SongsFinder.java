package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.GeniusSearchResponse;
import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.GeniusSongResponse;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SongsFinder {
    private final GeniusClient geniusClient;
    private final String accessToken;

    private Cache<String, GeniusSearchResponse> cache;

    public SongsFinder(GeniusClient geniusClient, @Value("${genius.api-client.access-token}") String accessToken) {
        this.geniusClient = geniusClient;
        this.accessToken = accessToken;
    }

    @PostConstruct
    public void init() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        this.cache = cacheManager.createCache("songs", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, GeniusSearchResponse.class, ResourcePoolsBuilder.heap(10_000)));
    }

    @SuppressWarnings(value = "unchecked")
    GeniusSearchResponse getSongs(String query) {
        if (!cache.containsKey(query)) {
            cache.put(query, geniusClient.search(query, "Bearer " + accessToken));
        }
        return cache.get(query);
    }

    public GeniusSongResponse song(Long id) {
        return geniusClient.song(id, "Bearer " + accessToken);
    }
}
