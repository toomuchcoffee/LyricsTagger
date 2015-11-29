package gui;

import javax.swing.*;

public class ProgressBar extends JProgressBar {
    private boolean showValues;

    public void setShowValues(boolean b) {
        showValues = b;
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();
        if (showValues) {
            sb.append(getValue());
            sb.append("/");
            sb.append(getMaximum());
            sb.append(" ");
        }
        sb.append(super.getString());
        return sb.toString();
    }
}
