package com.pratyush.core.model;

import java.util.Date;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name="DOCUMENT_EMBEDDING")
public class DocumentEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="document_id", referencedColumnName = "id")
    private Document document;

    private Integer chunk;

    @Column
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384)
    private float[] embedding;

    private Date created_date;

    public Long getId() {
        return this.id;
    }

    public Document getDocument() {
        return this.document;
    }

    public Integer getChunk() {
        return this.chunk;
    }

    public Date getCreatedDate() {
        return this.created_date;
    }

    public float[] getEmbedding() {
        return this.embedding;
    }

}
