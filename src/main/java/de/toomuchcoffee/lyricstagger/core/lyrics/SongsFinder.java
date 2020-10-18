package de.toomuchcoffee.lyricstagger.core.lyrics;

import com.google.inject.Inject;
import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.GeniusSearchResponse;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

class SongsFinder {
    private final GeniusClient geniusClient;

    private Cache<String, GeniusSearchResponse> cache;

    @Inject
    SongsFinder(GeniusClient geniusClient) {
        this.geniusClient = geniusClient;
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        this.cache = cacheManager.createCache("songs", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, GeniusSearchResponse.class, ResourcePoolsBuilder.heap(10_000)));
    }

    @SuppressWarnings(value = "unchecked")
    GeniusSearchResponse getSongs(String query) {
        if (!cache.containsKey(query)) {
            cache.put(query, geniusClient.search(query));
        }
        return cache.get(query);
    }
}
