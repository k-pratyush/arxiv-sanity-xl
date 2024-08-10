package com.pratyush.core.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pratyush.core.model.Document;
import com.pratyush.core.model.DocumentEmbedding;
import com.pratyush.core.model.DocumentEmbeddingProjection;
import com.pratyush.core.repository.DocumentEmbeddingRepository;
import com.pratyush.core.repository.DocumentRepository;

@Service
public class SearchService {
    @Autowired
    DocumentEmbeddingRepository documentEmbeddingRepository;

    @Autowired
    DocumentRepository documentRepository;

    public List<DocumentEmbeddingProjection> getTopMatching(Integer topN, double[] embedding) {
        List<DocumentEmbedding>matches = documentEmbeddingRepository.findTopMatching(embedding);
        Set<Long> visited = new HashSet<Long>();
        List<DocumentEmbeddingProjection> results = new ArrayList<>();

        for(DocumentEmbedding match: matches) {
            Document docObj = match.getDocument();
            if(visited.contains(docObj.getId()) == false) {
                visited.add(docObj.getId());
                results.add(new DocumentEmbeddingProjection(docObj.getUrl(), docObj.getName()));
            }
        }
        return results.subList(0, topN);
    }
}
