package com.bank.banking_system.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*; 

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // Spring Data JPA will automatically implement this method based on the method name convention
    Optional<UserEntity> findByEmail(String email); 
}
