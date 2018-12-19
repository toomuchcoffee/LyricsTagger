package de.toomuchcoffee.lyricstagger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.toomuchcoffee.lyricstagger.gui.Main;

import javax.swing.*;
import java.awt.*;
import java.util.logging.LogManager;

import static com.google.inject.util.Modules.EMPTY_MODULE;

public class Application {

    public static void main(String[] args) {
        LogManager.getLogManager().reset();

        Injector injector = Guice.createInjector(EMPTY_MODULE);

        JFrame frame = injector.getInstance(Main.class);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.pack();
        frame.setVisible(true);
    }
}