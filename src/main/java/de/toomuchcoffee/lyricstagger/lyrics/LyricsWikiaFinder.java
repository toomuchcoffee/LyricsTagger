package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class LyricsWikiaFinder {

    private QueryPermuter permuter = new QueryPermuter();
    private LyricsWikiaXmlParser xmlParser = new LyricsWikiaXmlParser();
    private LyricsWikiaHtmlParser htmlParser = new LyricsWikiaHtmlParser();

    public Optional<String> findLyrics(String artist, String song) {
        Optional<String> lyrics = findSongLyrics(artist, song);
        if (!lyrics.isPresent()) {
            lyrics = findMedleyLyrics(artist, song);
        }
        return lyrics;
    }

    private Optional<String> findSongLyrics(String artist, String song) {
        return permuter.permuteSongTitle(song).parallelStream()
                .map(s -> new Query(artist, s))
                .map(query -> xmlParser.findLyrics(query)
                        .map(url -> htmlParser.findLyrics(url))
                        .flatMap(f -> f))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(f -> f);
    }

    private Optional<String> findMedleyLyrics(String artist, String song) {
        if (song.contains("/")) {
            String[] parts = song.split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                Optional<String> lyrics = findSongLyrics(artist, part);
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
        return Optional.empty();
    }

}
