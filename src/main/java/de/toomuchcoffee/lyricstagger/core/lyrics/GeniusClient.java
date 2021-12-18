package de.toomuchcoffee.lyricstagger.core.lyrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "genius", url = "https://api.genius.com")
interface GeniusClient {
    @GetMapping(value = "/search", consumes = "application/json")
    SearchResult search(@RequestParam("q") String query, @RequestHeader("Authorization") String token);

    @GetMapping(value = "/songs/{id}", consumes = "application/json")
    SongResult song(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @Data
    class SearchResult {
        private Meta meta;
        private SearchResponse response;
    }

    @Data
    class SongResult {
        private Meta meta;
        private SongResponse response;
    }

    @Data
    class Meta {
        private int status;
    }

    @Data
    class SearchResponse {
        private int status;
        private List<Hit> hits;
    }

    @Data
    class SongResponse {
        private Song song;
    }

    @Data
    class Hit {
        private String type;
        private Result result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Result {
        private Long id;
        private String title;
        @JsonProperty("primary_artist")
        private Artist artist;
    }

    @Data
    class Song {
        private Long id;
        private String title;
        private String url;
    }

    @Data
    class Artist {
        private Long id;
        private String name;
    }
}
