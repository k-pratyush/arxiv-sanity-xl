package com.pratyush.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pratyush.core.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    @Query("select d from Document d order by d.created_date desc limit ?1")
    public List<Document> findTopNPapers(Integer topN);

}
