package com.bank.banking_system.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Service class that implements Spring Security's UserDetailsService to load user-specific data during authentication.
 * This class will interact with the UserRepository to fetch user details based on the email (username) provided during login.
 * It will convert the UserEntity from the database into a UserDetails object that Spring Security can use for authentication and authorization.
 */

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomerUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the user from the database using the email
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert UserEntity to UserDetails (this is a custom implementation of UserDetails)
        return User.builder()
            .username(userEntity.getEmail())
            .password(userEntity.getHashedPassword()) // The password should already be hashed in the database
            .roles(userEntity.getRole().replace("ROLE_", "")) // Spring Security adds "ROLE_" automatically
            .build();
    }
    
}
