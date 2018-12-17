package de.toomuchcoffee.lyricstagger.lyrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@Slf4j
class LyricsWikiaXmlParser {

    Optional<String> findLyrics(Query query)  {
        try {
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
                return Optional.ofNullable(lyricsUrlString);
            }
        } catch (Exception e) {
            log.warn("Failed to find lyrics for query {}", query, e);
            return Optional.empty();
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
}
