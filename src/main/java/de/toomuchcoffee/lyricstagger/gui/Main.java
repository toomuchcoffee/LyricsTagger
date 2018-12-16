package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.tagging.AudioFileRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.String.format;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;


@Slf4j
public class Main extends JFrame {

    private Table table;

    private ActionPanel actionPanel = new ActionPanel(this);
    private JProgressBar progress = new JProgressBar();

    @Getter
    @Setter
    private List<AudioFileRecord> records = new ArrayList<>();

    public Main(String title) {
        super(title);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel northPanel = new JPanel();
        panel.add(northPanel, BorderLayout.NORTH);

        northPanel.add(actionPanel);

        JPanel southPanel = new JPanel();
        progress.setStringPainted(true);
        progress.setIndeterminate(false);
        southPanel.add(progress);

        panel.add(southPanel, BorderLayout.SOUTH);

        table = new Table(this);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        getContentPane().add(panel);
    }

    <T> void processActionAndUpdateGUI(Collection<T> items, Function<T, Boolean> action, String message) {
        new Thread(() -> {
            actionPanel.disableAll();

            progress.setMaximum(items.size());
            progress.setValue(0);

            AtomicInteger count = new AtomicInteger();

            items.parallelStream().forEach(item -> {
                try {
                    if (action.apply(item)) {
                        count.getAndIncrement();
                    }
                    invokeLater(() -> {
                        table.repaint();
                        table.revalidate();
                    });
                } catch (Exception e) {
                    log.error("Action {} failed for item {}", action, item, e);
                }
                progress.setValue(progress.getValue() + 1);
            });

            actionPanel.next();
            showMessageDialog(this, format(message, count.get()));
        }).start();
    }

}