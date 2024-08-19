package com.pratyush.docsearch.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RankUtils {

    public List<String> getWordsFromLine(String line) {
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public List<String> getWordsFromLines(List<String> lines) {
        List<String> words = new ArrayList<>();
        for(String line: lines) {
            words.addAll(getWordsFromLine(line));
        }
        return words;
    }
}
