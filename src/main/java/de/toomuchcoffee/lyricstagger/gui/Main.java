package de.toomuchcoffee.lyricstagger.gui;

import de.toomuchcoffee.lyricstagger.core.record.AudioFileRecord;
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.lang.String.format;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;


@Slf4j
public class Main extends JFrame {

    private Table table;
    private ActionPanel actionPanel;

    @Getter
    @Setter
    private List<AudioFileRecord> records = new ArrayList<>();

    public Main(String title) {
        super(title);

        setSize(1024, 768);

        JPanel panel = new JPanel(new BorderLayout());

        actionPanel = new ActionPanel(this);
        panel.add(actionPanel, NORTH);

        table = new Table(this);
        panel.add(new JScrollPane(table), CENTER);

        getContentPane().add(panel);
    }

    <T> void processActionAndUpdateGUI(Collection<T> items, Function<T, Boolean> action, String message) {
        JProgressBar progress = new JProgressBar();
        progress.setMaximum(items.size());
        progress.setValue(0);

        final JDialog dialog = new JDialog(this, "Processing...", true);
        dialog.add(CENTER, progress);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 75);
        dialog.setLocationRelativeTo(this);

        progress.setStringPainted(true);
        progress.setIndeterminate(false);

        new Thread(() -> {
            invokeLater(() -> dialog.setVisible(true));

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
                    log.error("Action failed for item {}", item, e);
                    JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage());
                    System.exit(1);
                }
                progress.setValue(progress.getValue() + 1);
            });

            actionPanel.next();
            invokeLater(() -> dialog.setVisible(false));
            showMessageDialog(this, format(message, count.get()));
        }).start();
    }

}