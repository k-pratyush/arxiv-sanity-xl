package com.pratyush.docsearch.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Task implements Serializable {
    private final List<String> searchTerms;
    private final List<Long> documentIds;
    private final String rankingMethod;

    public Task(List<String> searchTerms, List<Long> documentIds, String rankingMethod) {
        this.searchTerms = searchTerms;
        this.documentIds = documentIds;
        this.rankingMethod = rankingMethod;
    }

    public List<String> getSearchTerms() {
        return Collections.unmodifiableList(this.searchTerms);
    }

    public List<Long> getDocumentIds() {
        return Collections.unmodifiableList(this.documentIds);
    }

    public String getRankingMethod() {
        return this.rankingMethod;
    }
}
