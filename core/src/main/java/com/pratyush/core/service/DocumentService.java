package com.pratyush.core.service;

import com.pratyush.core.model.Document;
import com.pratyush.core.model.DocumentEmbedding;
import com.pratyush.core.model.Users;

import org.springframework.stereotype.Service;

import com.pratyush.core.repository.DocumentEmbeddingRepository;
import com.pratyush.core.repository.DocumentRepository;
import com.pratyush.core.repository.UsersRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentEmbeddingRepository documentEmbeddingRepository;

    @Autowired
    private UsersRepository usersRepository;

    public List<Document> getTopNPapers(Integer topN) {
        return documentRepository.findTopNPapers(topN);
    }

    public List<Document> getAllPapers() {
        return documentRepository.findAll();
    }

    public List<DocumentEmbedding> getDocumentEmbeddings() {
        return documentEmbeddingRepository.findAll();
    }

    public List<Users> getUsers() {
        return usersRepository.findAll();
    }
}
