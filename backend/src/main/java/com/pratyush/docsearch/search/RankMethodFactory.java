package com.pratyush.docsearch.search;

public class RankMethodFactory {
    public static RankingMethod getRankingAlgorithm(String rankingMethod) {
        if(rankingMethod.equals("tfidf")) {
            return new TFIDF();
        } else {
            return new BM25();
        }
    }
}
