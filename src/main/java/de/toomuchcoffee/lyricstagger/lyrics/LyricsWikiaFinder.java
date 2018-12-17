package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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
        List<Query> queries = permuter.permuteSongTitle(song).stream()
                .map(s -> new Query(artist, s))
                .collect(toList());

        for (Query query : queries) {
            try {
                Optional<String> lyrics = Optional.empty();
                Optional<String> lyricsUrl = xmlParser.findLyrics(query);
                if (lyricsUrl.isPresent()) {
                    lyrics = htmlParser.findLyrics(lyricsUrl.get());
                }
                if (lyrics.isPresent()) {
                    return lyrics;
                }
            } catch (IOException | URISyntaxException | SAXException | ParserConfigurationException e) {
                log.warn("Failed to find lyrics for artist {} and song {}", artist, song, e);
            }
        }
        return Optional.empty();
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
