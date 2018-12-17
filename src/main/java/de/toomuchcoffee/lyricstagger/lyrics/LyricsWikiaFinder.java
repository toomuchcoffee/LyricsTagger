package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

@Slf4j
public class LyricsWikiaFinder {

    private static final Set<String> NOT_LICENSED = newHashSet(
            "Unfortunately, we are not licensed to display the full lyrics for this song at the moment.",
            "We don't currently have a license for these lyrics. Please try again in a few days!");

    private QueryPermuter permuter = new QueryPermuter();

    public String findLyrics(String artist, String song) {
        String lyrics = null;

        List<Query> queries = permuter.permuteSongTitle(song).stream()
                .map(s -> new Query(artist, s))
                .collect(toList());

        Iterator<Query> it = queries.iterator();
        while (it.hasNext() && lyrics == null) {
            Query query = it.next();
            try {
                lyrics = internalFindLyrics(query);
            } catch (IOException | URISyntaxException | SAXException | ParserConfigurationException e) {
                log.warn("Failed to find lyrics for artist {} and song {}", artist, song, e);
            }
        }

        if (lyrics == null) {
            Optional<String> mergedLyrics = mergedLyrics(new Query(artist, song));
            if (mergedLyrics.isPresent()) {
                return mergedLyrics.get();
            }
        }

        if (lyrics != null && lyrics.trim().length() == 0) {
            return null;
        }

        return lyrics;
    }

    private Optional<String> mergedLyrics(Query query) {
        if (query.getSong().contains("/")) {
            String[] parts = query.getSong().split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                String lyrics = findLyrics(query.getArtist(), part);
                if (lyrics == null) {
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

    @Getter
    @RequiredArgsConstructor
    private static class Query {
        private final String artist;
        private final String song;
    }

    private String internalFindLyrics(Query query) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        Document doc = getDocument(query.getArtist(), query.getSong());
        doc.getDocumentElement().normalize();
        Element lyricsElem = (Element) doc.getElementsByTagName("lyrics").item(0);
        if (lyricsElem == null) {
            return null;
        }
        String lyricsAbstract = lyricsElem.getTextContent();
        if (lyricsAbstract == null || lyricsAbstract.trim().length() == 0 || "not found".equalsIgnoreCase(lyricsAbstract)) {
            return null;
        } else if ("instrumental".equalsIgnoreCase(lyricsAbstract)) {
            return "Instrumental";
        } else {
            Element urlElem = (Element) doc.getElementsByTagName("url").item(0);
            String lyricsUrlString = urlElem.getTextContent();
            return parseHtml(lyricsUrlString);
        }
    }

    private Document getDocument(String artist, String song) throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
        URL url = buildUrl(artist, song);
        log.info("Request url: {}", url);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(url.openStream());
    }

    private URL buildUrl(String artist, String song) throws URISyntaxException, MalformedURLException {
        URIBuilder b = new URIBuilder("http://lyrics.wikia.com/api.php");
        b.addParameter("fmt", "xml");
        b.addParameter("func", "getSong");
        b.addParameter("artist", artist);
        b.addParameter("song", song);
        return b.build().toURL();
    }

    private String parseHtml(String urlString) throws IOException {
        URL url = new URL(urlString);
        LyricsWikiaHtmlParser htmlParser = new LyricsWikiaHtmlParser();
        InputStreamReader stream = new InputStreamReader(url.openStream());
        Reader reader = new BufferedReader(stream);
        new ParserDelegator().parse(reader, htmlParser, true/*ignore charset*/);
        String lyrics = htmlParser.getText();

        if (lyrics != null) {
            for (String licenseText : NOT_LICENSED) {
                if (lyrics.contains(licenseText)) {
                    return null;
                }
            }
        }
        return lyrics;
    }

}
