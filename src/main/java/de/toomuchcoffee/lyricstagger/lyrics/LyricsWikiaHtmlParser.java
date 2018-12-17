package de.toomuchcoffee.lyricstagger.lyrics;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

class LyricsWikiaHtmlParser extends HTMLEditorKit.ParserCallback {
    private static final Set<String> NOT_LICENSED = newHashSet(
            "Unfortunately, we are not licensed to display the full lyrics for this song at the moment.",
            "We don't currently have a license for these lyrics. Please try again in a few days!");

    private StringBuilder sb = new StringBuilder();
    private boolean reachedLyricsBox;
    private int openDivCountInsideLyricsBox = 0;

    public Optional<String> findLyrics(String urlString) throws IOException {
        URL url = new URL(urlString);
        InputStreamReader stream = new InputStreamReader(url.openStream());
        Reader reader = new BufferedReader(stream);
        new ParserDelegator().parse(reader, this, true);
        String lyrics = getText().trim();

        for (String licenseText : NOT_LICENSED) {
            if (lyrics.contains(licenseText)) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(lyrics.length() == 0 ? null : lyrics);
    }

    @Override
    public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (HTML.Tag.BR.equals(tag) && shouldReadText()) {
            sb.append("\n");
        }
        super.handleSimpleTag(tag, a, pos);
    }

    @Override
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet atts, int pos) {
        if (HTML.Tag.DIV.equals(tag)) {
            if (reachedLyricsBox)
                openDivCountInsideLyricsBox++;
            else if (atts.containsAttribute(HTML.Attribute.CLASS, "lyricbox")) {
                this.reachedLyricsBox = true;
            }
        }

        super.handleStartTag(tag, atts, pos);
    }

    @Override
    public void handleEndTag(HTML.Tag tag, int pos) {
        if (HTML.Tag.DIV.equals(tag)) {
            if (reachedLyricsBox && openDivCountInsideLyricsBox > 0)
                openDivCountInsideLyricsBox--;
            else
                reachedLyricsBox = false;
        }
        super.handleEndTag(tag, pos);
    }

    @Override
    public void handleText(char[] data, int pos) {
        if (shouldReadText()) {
            sb.append(data);
        }
    }

    private boolean shouldReadText() {
        return reachedLyricsBox && openDivCountInsideLyricsBox <= 0;
    }

    private String getText() {
        return sb.toString();
    }
}
