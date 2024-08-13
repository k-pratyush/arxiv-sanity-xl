package com.pratyush.core.model;

public class DocumentStats {
    private Long documentId;
    private Double score;

    public DocumentStats(Long documentId, Double score) {
        this.documentId = documentId;
        this.score = score;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Double getScore() {
        return score;
    }
}
