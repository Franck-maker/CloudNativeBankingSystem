package com.bank.banking_system.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; 

public interface SpringDataJpaAccountRepository extends JpaRepository<AccountJpaEntity, UUID>{
    
    
}
