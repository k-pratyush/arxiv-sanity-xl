package com.pratyush.core.service;

import java.util.List;

import com.pratyush.core.model.Document;
import com.pratyush.core.model.DocumentEmbedding;

import com.pratyush.core.repository.DocumentEmbeddingRepository;
import com.pratyush.core.repository.DocumentRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentEmbeddingRepository documentEmbeddingRepository;

    public List<Document> getTopNPapers(Integer topN) {
        return documentRepository.findTopNPapers(topN);
    }

    public List<Document> getAllPapers() {
        return documentRepository.findAll();
    }

    public List<DocumentEmbedding> findAll() {
        return documentEmbeddingRepository.findAll();
    }
}
