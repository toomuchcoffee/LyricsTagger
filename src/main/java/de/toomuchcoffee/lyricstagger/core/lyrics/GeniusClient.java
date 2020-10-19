package de.toomuchcoffee.lyricstagger.core.lyrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@FeignClient(name = "genius", url = "https://api.genius.com")
interface GeniusClient {
    @RequestMapping(method = GET, value = "/search?q={query}")
    GeniusSearchResponse search(@PathVariable("query") String query, @RequestHeader("Authorization") String token);

    @RequestMapping(method = GET, value = "/songs/{id}")
    GeniusSongResponse song(@PathVariable("id") Long id,@RequestHeader("Authorization") String token);

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
    public static class Meta {
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
        @JsonProperty("primary_artist")
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
