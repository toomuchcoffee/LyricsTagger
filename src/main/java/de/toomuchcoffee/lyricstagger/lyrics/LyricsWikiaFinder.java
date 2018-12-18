package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparingInt;

@Slf4j
public class LyricsWikiaFinder {

    private LyricsWikiaJsonParser jsonParser = new LyricsWikiaJsonParser();
    private LyricsWikiaHtmlParser htmlParser = new LyricsWikiaHtmlParser();

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
        return findMostSimilarTerm(jsonParser.findSongs(artist), song)
                .map(query -> jsonParser.findLyrics(artist, query)
                        .map(htmlParser::findLyrics)
                        .flatMap(f -> f))
                .filter(Optional::isPresent)
                .flatMap(f -> f);
    }

    private Optional<String> findMostSimilarTerm(Set<String> pool, String q) {
        return pool.stream().min(comparingInt(p -> LevenshteinDistance.getDefaultInstance().apply(p, q)));
    }

}
