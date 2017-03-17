package de.toomuchcoffee.itags.lyrics;

import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;


public class LyricsWikiaFinder {

    public static final String NOT_LICENSED = "Unfortunately, we are not licensed to display the full lyrics for this song at the moment.";

    public static String findLyrics(String artist, String song) {
        try {
            String urlString = buildUrl(artist, song);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(urlString);
            doc.getDocumentElement().normalize();
            Element lyricsElem = (Element) doc.getElementsByTagName("lyrics").item(0);
            String lyricsAbstract = lyricsElem.getTextContent();
            if (lyricsAbstract == null || lyricsAbstract.trim().length() == 0 ||
                    "not found".equalsIgnoreCase(lyricsAbstract)) {
                return null;
            } else if ("instrumental".equalsIgnoreCase(lyricsAbstract)) {
                return "Instrumental";
            } else {
                Element urlElem = (Element) doc.getElementsByTagName("url").item(0);
                String lyricsUrlString = urlElem.getTextContent();
                return parseHtml(lyricsUrlString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}

    private static String buildUrl(String artist, String song) throws URISyntaxException {
        URIBuilder b = new URIBuilder("http://lyrics.wikia.com/api.php");
        b.addParameter("fmt", "xml");
        b.addParameter("func", "getSong");
        b.addParameter("artist", artist);
        b.addParameter("song", song);
        return b.toString();
    }

    private static String parseHtml(String urlString) throws IOException {
		URL url = new URL(urlString);
		LyricsWikiaHtmlParser htmlParser = new LyricsWikiaHtmlParser();
		InputStreamReader stream = new InputStreamReader(url.openStream());
		Reader reader = new BufferedReader(stream);
		new ParserDelegator().parse(reader, htmlParser, true/*ignore charset*/);
		String lyrics = htmlParser.getText();
		
		if (lyrics!=null && lyrics.contains(NOT_LICENSED)) {
            return null;
        } else {
            return lyrics;
        }
	}

}
