package de.toomuchcoffee.lyricstagger.lyrics;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class QueryPermuter {

    private static final Pattern ENCLOSED_WITH_PARENTHESES_OR_BRACKETS = compile("[(\\[][^()]*?[)\\]]");
    private static final Pattern PUNCTUATION = compile("[!?.]");
    private static final Pattern AND = compile("\\band\\b");
    private static final Pattern AMPERSAND = compile("[&]");

    private Map<Pattern, String> REG_EXES_AND_REPLACEMENT = ImmutableMap.of(
            ENCLOSED_WITH_PARENTHESES_OR_BRACKETS, "",
            PUNCTUATION, "",
            AND, "&",
            AMPERSAND, "and"
    );


    public List<String> permuteSongTitle(String songTitle) {
        songTitle = songTitle.toLowerCase();
        List<String> permutations = new ArrayList<>();
        permutations.add(songTitle);

        for (Map.Entry<Pattern, String> entry : REG_EXES_AND_REPLACEMENT.entrySet()) {
            Pattern regex = entry.getKey();
            Matcher m = regex.matcher(songTitle);
            if (m.find()) {
                permutations.add(songTitle.replaceAll(regex.toString(), entry.getValue()));
            }
        }

        return permutations;
    }
}
