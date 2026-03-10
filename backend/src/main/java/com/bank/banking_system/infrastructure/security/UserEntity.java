package com.bank.banking_system.infrastructure.security;

import jakarta.persistence.*; 
import java.util.*; 

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId; 

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String Hashedpassword; //This will store the BCrypt hashed password

    @Column(nullable = false)
    private String role; // ADMIN, USER.

    // Default constructor for JPA
    public UserEntity() {}

    public UserEntity(String email, String hashedPassword, String role) {
        this.email = email;
        this.Hashedpassword = hashedPassword;
        this.role = role;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId; 
    } 
    public String getEmail() {
        return email; 
    }
    public String getHashedPassword() {
        return Hashedpassword; 
    }
    public String getRole() {
        return role; 
    }
    public void setUserId(UUID userId) {
        this.userId = userId; 
    }
    public void setEmail(String email) {
        this.email = email; 
    }
    public void setHashedPassword(String hashedPassword) {
        this.Hashedpassword = hashedPassword; 
    }
}
