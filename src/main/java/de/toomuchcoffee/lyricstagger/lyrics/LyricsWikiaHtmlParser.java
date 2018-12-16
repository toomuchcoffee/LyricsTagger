package de.toomuchcoffee.lyricstagger.lyrics;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

class LyricsWikiaHtmlParser extends HTMLEditorKit.ParserCallback {
    private StringBuilder sb = new StringBuilder();
    private boolean reachedLyricsBox;
    private int openDivCountInsideLyricsBox = 0;

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

    String getText() {
        return sb.toString();
    }
}
