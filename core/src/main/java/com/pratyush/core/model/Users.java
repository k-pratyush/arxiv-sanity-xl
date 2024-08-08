package com.pratyush.core.model;

import java.util.Date;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "USERS")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", unique = true)
    private String user_id;

    private String preferences;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384)
    private double[] preference_vector;
    private Date created_date;

    public Long getId() {
        return this.id;
    }

    public String getUserId() {
        return this.user_id;
    }

    public double[] getPreferenceVector() {
        return this.preference_vector;
    }

    public String preferences() {
        return this.preferences;
    }

    public Date getCreatedDate() {
        return this.created_date;
    }
}
