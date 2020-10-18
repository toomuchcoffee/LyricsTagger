package de.toomuchcoffee.lyricstagger.core.lyrics;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

@Data
@NoArgsConstructor
public class LyricsTaggerProperties {
    private Genius genius;

    @Data
    @NoArgsConstructor
    public static class Genius {
        private ApiClient apiClient;
    }

    @Data
    @NoArgsConstructor
    public static class ApiClient {
        private String id;
        private String secret;
        private String accessToken;
    }

    public static ApiClient loadCredentials() {
        Yaml yaml = new Yaml(new Constructor(LyricsTaggerProperties.class));
        InputStream inputStream = GeniusClient.class
                .getClassLoader()
                .getResourceAsStream("application.yml");
        LyricsTaggerProperties obj = yaml.load(inputStream);
        return obj.getGenius().getApiClient();
    }
}


