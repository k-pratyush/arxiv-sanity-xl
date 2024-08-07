package com.pratyush.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pratyush.core.model.DocumentEmbedding;

public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {
    
}
