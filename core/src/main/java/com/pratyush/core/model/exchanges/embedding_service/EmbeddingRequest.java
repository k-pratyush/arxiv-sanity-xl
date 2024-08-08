package com.pratyush.core.model.exchanges.embedding_service;

public class EmbeddingRequest {
    public String query;

    public EmbeddingRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }
}
