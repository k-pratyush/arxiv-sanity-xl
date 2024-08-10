package com.pratyush.docsearch.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
    private Map<Long, DocumentData> documentIdToDocumentData = new HashMap<>();

    public void addDocumentData(Long documentId, DocumentData documentData) {
        this.documentIdToDocumentData.put(documentId, documentData);
    }

    public Map<Long, DocumentData> getDocumentToDocumentData() {
        return Collections.unmodifiableMap(this.documentIdToDocumentData);
    }
}
