package com.bank.banking_system.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bank.banking_system.infrastructure.persistence.AccountJpaEntity;
import com.bank.banking_system.infrastructure.persistence.SpringDataJpaAccountRepository;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final UserRepository userRepository;
    private final SpringDataJpaAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, SpringDataJpaAccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
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
        } else {
            log.info("SECURITY EVENT: Users already exist.");
        }

        // SEED BANK ACCOUNTS
        if(accountRepository.count() == 0) {

            // Create Mock bank Accounts for UI Testing
            log.info("DOMAIN EVENT: Injecting mock bank acounts for testing...");
            AccountJpaEntity adminAccount = new AccountJpaEntity(java.util.UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "Admin Corporate Account", new java.math.BigDecimal("10000.00"));
            AccountJpaEntity userAccount = new AccountJpaEntity(java.util.UUID.fromString("3f9f3c2e-6c3a-4c7c-b3c4-8a2d9b8e1f72"),"Standard User Checking", new java.math.BigDecimal("500.00"));
            accountRepository.save(adminAccount);
            accountRepository.save(userAccount);

            
        } else {
            log.info("DOMAIN EVENT: Accounts already exist.");
        }
    }
    
}
