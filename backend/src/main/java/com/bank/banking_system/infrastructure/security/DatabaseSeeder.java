package com.bank.banking_system.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed if the database is empty
        if(userRepository.count() == 0) {
            log.info("SECURITY EVENT: Initializing database with default users...");

            // Create the ADMIN user
            UserEntity admin = new UserEntity(
                "admin@bank.local",
                passwordEncoder.encode("admin123"), // Securely hash the password!
                "ADMIN"
            );
            userRepository.save(admin);

            // Create the USER user
            UserEntity user = new UserEntity(
                "user@bank.local",
                passwordEncoder.encode("password123"), // Securely hash the password!
                "USER"
            );
            userRepository.save(user);

            log.info("SECURITY EVENT: Database seeding completed.");
        } else {
            log.info("SECURITY EVENT: Database already contains users. Skipping seeding.");
        }
    }
    
}
