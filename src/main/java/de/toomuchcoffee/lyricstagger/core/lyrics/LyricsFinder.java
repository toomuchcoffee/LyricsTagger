package de.toomuchcoffee.lyricstagger.core.lyrics;

import com.google.common.annotations.VisibleForTesting;
import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.GeniusSearchResponse;
import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class LyricsFinder {

    private static final double LEVENTSHTEIN_THRESHOLD_RATIO = 0.3;

    private final GeniusLyricsScraper lyricsScraper;
    private final SongsFinder songsFinder;

    public Optional<String> findLyrics(String song, String artist) {
        if (song.contains("/")) {
            String[] parts = song.split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                Optional<String> lyrics = internalFindLyrics(part, artist);
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
        return internalFindLyrics(song, artist);
    }

    private Optional<String> internalFindLyrics(String song, String artist) {
        GeniusSearchResponse searchResponse = songsFinder.getSongs(artist + " " + song);
        Set<Result> pool = searchResponse.getResponse().getHits().stream()
                .filter(hit -> hit.getResult().getArtist().getName().equalsIgnoreCase(artist))
                .map(GeniusClient.Hit::getResult).collect(toSet());

        return findMostSimilarSongTitle(pool, song, (int) (song.length() * LEVENTSHTEIN_THRESHOLD_RATIO))
                .map(result -> songsFinder.song(result.getId()))
                .map(songResponse -> lyricsScraper.findLyrics(songResponse.getResponse().getSong().getUrl()))
                .filter(Optional::isPresent)
                .flatMap(f -> f);
    }

    @VisibleForTesting
    Optional<Result> findMostSimilarSongTitle(Set<Result> pool, String q, int threshold) {
        return pool.stream()
                .map(p -> new ResultWithDistance(p, LevenshteinDistance.getDefaultInstance().apply(p.getTitle(), q)))
                .filter(s -> s.getDistance() <= threshold)
                .sorted(Comparator.comparing(ResultWithDistance::getDistance))
                .map(ResultWithDistance::getValue)
                .findFirst();
    }

    @Getter
    @RequiredArgsConstructor
    private class ResultWithDistance {
        private final Result value;
        private final int distance;
    }

}
