package de.toomuchcoffee.lyricstagger.lyrics;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Set;

class SongsFinder {
    private LyricsWikiaJsonParser jsonParser;
    private Cache<String, Set> cache;

    SongsFinder(LyricsWikiaJsonParser jsonParser) {
        this.jsonParser = jsonParser;
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        this.cache = cacheManager.createCache("songs", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Set.class, ResourcePoolsBuilder.heap(10_000)));
    }

    @SuppressWarnings(value = "unchecked")
    Set<String> getSongs(String artist) {
        if (!cache.containsKey(artist)) {
            cache.put(artist, jsonParser.findSongs(artist));
        }
        return cache.get(artist);
    }
}
