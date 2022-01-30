package de.toomuchcoffee.lyricstagger.core.lyrics;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class GeniusLyricsScraper {
    Optional<String> findLyrics(String urlString) {
        try {
            Connection connection = Jsoup.connect(urlString);
            connection.userAgent("Mozilla");
            Document doc = connection.get();

            doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
            doc.select("br").append("\\n");
            doc.select("p").prepend("\\n\\n");

            Element body = doc.body();
            Elements lyricbox = body.getElementsByAttributeValueStarting("class", "lyrics");

            Elements message = lyricbox.select("div[data-lyrics-container=true]");
            if (message.isEmpty()) {
                message = lyricbox.select("div[class*=Message]");
            }

            String text = Jsoup.parse(message.html()).text();
            String s = text.replaceAll("\\\\n", "\n");

            String clean = Jsoup.clean(s, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));

            return Optional.of((clean.trim()));
        } catch (IOException e) {
            log.warn("Failed to find lyrics for url {}", urlString, e);
            return Optional.empty();
        }
    }

}
