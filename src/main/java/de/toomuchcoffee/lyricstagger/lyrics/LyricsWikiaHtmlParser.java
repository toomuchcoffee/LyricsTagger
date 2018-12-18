package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
class LyricsWikiaHtmlParser {
    private static final Set<String> NOT_LICENSED = newHashSet(
            "Unfortunately, we are not licensed to display the full lyrics for this song at the moment.",
            "We don't currently have a license for these lyrics. Please try again in a few days!");

    Optional<String> findLyrics(String urlString) {
        try {
            URL url = new URL(urlString);

            log.debug("Receiving XML from url {}", url);

            Scanner scanner = new Scanner(url.openStream(), UTF_8.name()).useDelimiter("\\A");
            String out = scanner.next();
            scanner.close();

            Document doc = Jsoup.parseBodyFragment(out);
            Element body = doc.body();
            Elements lyricbox = body.getElementsByAttributeValue("class", "lyricbox");

            String html = lyricbox.html();

            html = html.replaceAll("\n", "@@");
            String text = Jsoup.parse(html).text();
            String lyrics = text.replaceAll("@@", "\n").trim();

            for (String licenseText : NOT_LICENSED) {
                if (lyrics.contains(licenseText)) {
                    return Optional.empty();
                }
            }

            return Optional.of(lyrics);
        } catch (IOException e) {
            log.warn("Failed to find lyrics for url {}", urlString, e);
            return Optional.empty();
        }
    }

}
