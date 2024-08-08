package com.pratyush.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pratyush.core.model.DocumentEmbedding;

public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {
    
    // order by de.embedding <-> cast(:embedding as vector)
    @Query(nativeQuery = true,
        value = "select de.document_id, de.id, de.chunk, de.embedding, de.created_date from document_embedding de order by de.embedding <-> cast(:embedding as vector)")
    public List<DocumentEmbedding> findTopMatching(double[] embedding);
}
