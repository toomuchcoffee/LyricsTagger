package de.toomuchcoffee.lyricstagger.core.lyrics;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.Math.max;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class LyricsFinder {

    static final double LEVENTSHTEIN_SONG_THRESHOLD_RATIO = 0.30;
    static final double LEVENTSHTEIN_ARTIST_THRESHOLD_RATIO = 0.42;

    private final GeniusLyricsScraper lyricsScraper;
    private final SongFinder songFinder;

    public Optional<String> findLyrics(String song, String artist) {
        if (song.contains("/")) {
            String[] parts = song.split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                Optional<String> lyrics = internalFindLyrics(part, artist);
                if (lyrics.isEmpty()) {
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
        Set<Result> pool = songFinder.search(artist + " " + song).map(result -> result.getResponse().getHits().stream()
                .map(GeniusClient.Hit::getResult)
                .filter(hitResult -> matchArtistName(artist, hitResult.getArtist().getName())).collect(toSet()))
                .orElse(new HashSet<>());

        return findMostSimilarSongTitle(pool, song)
                .map(result -> songFinder.song(result.getId())
                        .map(r -> r.getResponse().getSong().getUrl()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(lyricsScraper::findLyrics);
    }

    public boolean matchArtistName(String artist, String hit) {
        Integer distance = LevenshteinDistance.getDefaultInstance().apply(hit, artist);
        return distance <= calculateThreshold(artist, hit, LEVENTSHTEIN_ARTIST_THRESHOLD_RATIO);
    }

    Optional<Result> findMostSimilarSongTitle(Set<Result> pool, String q) {
        return pool.stream()
                .map(p -> new ResultWithDistance(p, LevenshteinDistance.getDefaultInstance().apply(p.getTitle(), q)))
                .filter(s -> s.getDistance() <= calculateThreshold(s.getValue().getTitle(), q, LEVENTSHTEIN_SONG_THRESHOLD_RATIO))
                .sorted(Comparator.comparing(ResultWithDistance::getDistance))
                .map(ResultWithDistance::getValue)
                .findFirst();
    }

    private double calculateThreshold(String a, String b, double ratio) {
        if (a.length() < 6) {
            return 0;
        } else if (a.length() < 8) {
            return 1;
        } else if (a.length() < 10) {
            return 2;
        }
        return max(a.length(), b.length()) * ratio;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ResultWithDistance {
        private final Result value;
        private final double distance;
    }

}
