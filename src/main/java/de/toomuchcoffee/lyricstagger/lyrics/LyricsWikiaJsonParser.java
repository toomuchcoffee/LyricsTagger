package de.toomuchcoffee.lyricstagger.lyrics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
class LyricsWikiaJsonParser {
    private static final String GET_SONG = "getSong";
    private static final String GET_ARTIST = "getArtist";

    Optional<String> findLyrics(String artist, String song) {
        try {
            URL url = buildUrl(GET_SONG, ImmutableMap.of("artist", artist, "song", song));
            LyricsResult lyricsResult = getJson(url, LyricsResult.class);
            String lyrics = lyricsResult.getLyrics();
            if (lyrics == null || lyrics.trim().length() == 0 || "not found".equalsIgnoreCase(lyrics)) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(lyricsResult.getUrl());
            }
        } catch (Exception e) {
            log.warn("Failed to find lyrics for artist {} and song {}", artist, song, e);
            return Optional.empty();
        }
    }

    Set<String> findSongs(String artist) {
        try {
            URL url = buildUrl(GET_ARTIST, ImmutableMap.of("artist", artist));
            Artist foundArtist = getJson(url, Artist.class);
            return foundArtist.getAlbums().stream()
                    .map(Album::getSongs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Failed to find songs for artist {}", artist, e);
            return Sets.newHashSet();
        }
    }

    private URL buildUrl(String func, Map<String, String> params) throws URISyntaxException, MalformedURLException {
        URIBuilder b = new URIBuilder("http://lyrics.wikia.com/api.php");
        b.addParameter("fmt", "json");
        b.addParameter("func", func);
        params.forEach(b::addParameter);
        return b.build().toURL();
    }

    private <T> T getJson(URL url, Class<T> clazz) throws IOException {
        Scanner scanner = new Scanner(url.openStream(), UTF_8.name()).useDelimiter("\\A");
        String out = scanner.next();
        scanner.close();

        String json = out.substring(out.indexOf("{"));
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    @Data
    private static class LyricsResult {
        private String artist;
        private String song;
        private String lyrics;
        private String url;
    }

    @Data
    private static class Album {
       private String album;
       private String year;
       private List<String> songs;
    }

    @Data
    private static class Artist {
        private String artist;
        private List<Album> albums;
    }
}
