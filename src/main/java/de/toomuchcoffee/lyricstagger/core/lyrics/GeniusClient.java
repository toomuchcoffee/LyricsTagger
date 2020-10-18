package de.toomuchcoffee.lyricstagger.core.lyrics;

import com.google.gson.annotations.SerializedName;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static de.toomuchcoffee.lyricstagger.core.lyrics.LyricsTaggerProperties.loadCredentials;

@Slf4j
class GeniusClient {

    private static final String BASE_URL = "https://api.genius.com";
    private final Genius genius;
    private final String auth;

    interface Genius {
        @RequestLine("GET /search?q={query}")
        @Headers("Authorization: {auth}")
        GeniusSearchResponse search(@Param("query") String query, @Param("auth") String auth);

        @RequestLine("GET /songs/{id}")
        @Headers("Authorization: {auth}")
        GeniusSongResponse song(@Param("id") Long id, @Param("auth") String auth);
    }

    public GeniusClient() {
        this.genius = Feign.builder()
                .decoder(new GsonDecoder())
                .target(Genius.class, BASE_URL);
        this.auth = "Bearer " + loadCredentials().getAccessToken();
    }

    public GeniusSearchResponse search(String query) {
        return genius.search(query, auth);
    }

    public GeniusSongResponse song(Long id) {
        return genius.song(id, auth);
    }

    @Data
    public static class GeniusSearchResponse {
        private Meta meta;
        private SearchResponse response;
    }

    @Data
    public static class GeniusSongResponse {
        private Meta meta;
        private SongResponse response;
    }

    @Data
    private static class Meta {
        private int status;
    }

    @Data
    public static class SearchResponse {
        private int status;
        private List<Hit> hits;
    }

    @Data
    public static class SongResponse {
        private Song song;
    }

    @Data
    public static class Hit {
        private String type;
        private Result result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private Long id;
        private String title;
        @SerializedName("primary_artist")
        private Artist artist;
    }

    @Data
    public static class Song {
        private Long id;
        private String title;
        private String url;
    }

    @Data
    public static class Artist {
        private Long id;
        private String name;
    }
}
