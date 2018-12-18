package de.toomuchcoffee.lyricstagger.lyrics;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;

@Slf4j
public class Finder {

    private static final double LEVENTSHTEIN_THRESHOLD_RATIO = 0.3;

    private LyricsWikiaJsonParser jsonParser = new LyricsWikiaJsonParser();
    private LyricsWikiaHtmlParser htmlParser = new LyricsWikiaHtmlParser();

    private Map<String, Set<String>> cache;

    public void reset() {
        cache = new HashMap<>();
    }

    public Optional<String> findLyrics(String artist, String song) {
        if (song.contains("/")) {
            String[] parts = song.split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                Optional<String> lyrics = internalFindLyrics(artist, part);
                if (!lyrics.isPresent()) {
                    return Optional.empty();
                }
                sb.append("\n\n\n");
                sb.append(part.trim().toUpperCase());
                sb.append("\n\n");
                sb.append(lyrics);
            }
            return Optional.of(sb.toString().trim());
        }
        return internalFindLyrics(artist, song);
    }

    private Optional<String> internalFindLyrics(String artist, String song) {
        if (cache.get(artist) == null) {
            cache.put(artist, jsonParser.findSongs(artist));
        }
        return findMostSimilarTerm(cache.get(artist), song, (int) (song.length() * LEVENTSHTEIN_THRESHOLD_RATIO))
                .map(query -> jsonParser.findLyrics(artist, query)
                        .map(htmlParser::findLyrics)
                        .flatMap(f -> f))
                .filter(Optional::isPresent)
                .flatMap(f -> f);
    }

    @VisibleForTesting
    Optional<String> findMostSimilarTerm(Set<String> pool, String q, int threshold) {
        return pool.stream()
                .map(p -> new StringWithDistance(p, LevenshteinDistance.getDefaultInstance().apply(p, q)))
                .filter(s -> s.getDistance() <= threshold)
                .sorted(Comparator.comparing(StringWithDistance::getDistance))
                .map(StringWithDistance::getValue)
                .findFirst();
    }

    @Getter
    @RequiredArgsConstructor
    private class StringWithDistance {
        private final String value;
        private final int distance;
    }

}
