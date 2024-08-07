package com.pratyush.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pratyush.core.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
    
}
