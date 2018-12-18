package de.toomuchcoffee.lyricstagger;

import de.toomuchcoffee.lyricstagger.gui.Main;

import javax.swing.*;
import java.awt.*;
import java.util.logging.LogManager;

public class Application {

    public static void main(String[] args) {
        LogManager.getLogManager().reset();

        JFrame frame = new Main("Add lyrics to your music library");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.pack();
        frame.setVisible(true);
    }
}