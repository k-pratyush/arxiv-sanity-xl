package com.pratyush.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pratyush.core.repository.DocumentEmbeddingRepository;
import com.pratyush.core.repository.DocumentRepository;

@Service
public class SearchService {
    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentEmbeddingRepository documentEmbeddingRepository;

}
