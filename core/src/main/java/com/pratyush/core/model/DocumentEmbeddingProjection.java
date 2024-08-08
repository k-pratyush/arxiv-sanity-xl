package com.pratyush.core.model;

public class DocumentEmbeddingProjection {
    private String url;
    private String name;

    public DocumentEmbeddingProjection(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getDocumentUrl() {
        return url;
    }

    public String getDocumentName() {
        return name;
    }
}
