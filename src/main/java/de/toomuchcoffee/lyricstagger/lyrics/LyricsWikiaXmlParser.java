package de.toomuchcoffee.lyricstagger.lyrics;

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
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Slf4j
public class LyricsWikiaXmlParser {

    private static final Set<String> NOT_LICENSED = newHashSet(
            "Unfortunately, we are not licensed to display the full lyrics for this song at the moment.",
            "We don't currently have a license for these lyrics. Please try again in a few days!");

    private final LyricsWikiaHtmlParser htmlParser = new LyricsWikiaHtmlParser();

    public Optional<String> findLyrics(Query query) throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        Document doc = getDocument(query.getArtist(), query.getSong());
        doc.getDocumentElement().normalize();
        Element lyricsElem = (Element) doc.getElementsByTagName("lyrics").item(0);
        if (lyricsElem == null) {
            return Optional.empty();
        }
        String lyricsAbstract = lyricsElem.getTextContent();
        if (lyricsAbstract == null || lyricsAbstract.trim().length() == 0 || "not found".equalsIgnoreCase(lyricsAbstract)) {
            return Optional.empty();
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

    private Optional<String> parseHtml(String urlString) throws IOException {
        URL url = new URL(urlString);
        InputStreamReader stream = new InputStreamReader(url.openStream());
        Reader reader = new BufferedReader(stream);
        new ParserDelegator().parse(reader, htmlParser, true);
        String lyrics = htmlParser.getText().trim();

        for (String licenseText : NOT_LICENSED) {
            if (lyrics.contains(licenseText)) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(lyrics.length() == 0 ? null : lyrics);
    }

}
