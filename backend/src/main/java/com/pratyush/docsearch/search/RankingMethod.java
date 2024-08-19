package com.pratyush.docsearch.search;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import com.pratyush.docsearch.model.DocumentData;

public interface RankingMethod {
    public DocumentData createDocumentData(List<String> words, List<String> terms);
    public List<String> getWordsFromLine(String line);
    public NavigableMap<Double, List<Long>> getDocumentsSortedByScore(List<String> terms, Map<Long, DocumentData> documentResults);
}
