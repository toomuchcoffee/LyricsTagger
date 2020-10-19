package de.toomuchcoffee.lyricstagger;

import de.toomuchcoffee.lyricstagger.gui.MainFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

@SpringBootApplication
@EnableFeignClients
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(Application.class)
                .headless(false).run(args);

        EventQueue.invokeLater(() -> {
            MainFrame frame = applicationContext.getBean(MainFrame.class);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(1024, 768));
            frame.pack();
            frame.setVisible(true);
        });
    }

}