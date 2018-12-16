package de.toomuchcoffee.lyricstagger;

import de.toomuchcoffee.lyricstagger.gui.Main;

import javax.swing.*;

public class Application {

    public static void main(String[] args) {
        JFrame frame = new Main("Add lyrics to your music library");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}