package de.toomuchcoffee.lyricstagger.config;

import de.toomuchcoffee.lyricstagger.core.lyrics.GeniusClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Bean
    public GeniusClient geniusClient() {
        // Configure the base URL for the Genius API
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.genius.com")
                .build();

        // Wrap it into an adapter and factory
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        // Generate the working client bean implementation
        return factory.createClient(GeniusClient.class);
    }
}