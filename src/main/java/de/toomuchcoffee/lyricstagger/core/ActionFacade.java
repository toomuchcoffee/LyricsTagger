package de.toomuchcoffee.lyricstagger.core;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.toomuchcoffee.lyricstagger.core.lyrics.LyricsFinder;
import de.toomuchcoffee.lyricstagger.core.record.AudioFileRecord;
import de.toomuchcoffee.lyricstagger.core.record.Reader;
import de.toomuchcoffee.lyricstagger.core.record.Writer;

import java.io.File;
import java.util.Optional;

import static com.google.inject.util.Modules.EMPTY_MODULE;

public class ActionFacade {

    private final Reader reader;
    private final LyricsFinder lyricsFinder;
    private final Writer writer;

    private static final Injector INJECTOR = Guice.createInjector(EMPTY_MODULE);

    public static final ActionFacade INSTANCE = INJECTOR.getInstance(ActionFacade.class);

    @Inject
    public ActionFacade(LyricsFinder lyricsFinder, Reader reader, Writer writer) {
        this.lyricsFinder = lyricsFinder;
        this.reader = reader;
        this.writer = writer;
    }

    public Optional<AudioFileRecord> readFile(File file, boolean overwrite) {
        return reader.readFile(file, overwrite);
    }

    public Optional<String> findLyrics(String artist, String song) {
        return lyricsFinder.findLyrics(artist, song);
    }

    public void writeLyrics(File file, String lyrics) {
        writer.writeToFile(file, lyrics);
    }


}
