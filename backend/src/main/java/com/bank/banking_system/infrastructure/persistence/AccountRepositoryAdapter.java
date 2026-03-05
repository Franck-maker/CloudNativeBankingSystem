package com.bank.banking_system.infrastructure.persistence;
import java.util.*;

import org.springframework.stereotype.Component;

import com.bank.banking_system.application.ports.out.AccountRepositoryPort;
import com.bank.banking_system.domain.model.Account;



/**
 * this class implements our Use Case Port
 * talks to the Spring Data JPA repository to perform database operations
 * and maps the data back and forth. This way we keep our domain model clean and decoupled from the persistence layer.
 */
@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final SpringDataJpaAccountRepository jpaRepository;

    public AccountRepositoryAdapter(SpringDataJpaAccountRepository jpaRepository){
        this.jpaRepository = jpaRepository; 
    }

    @Override
    public Account save(Account account) {
        //Map Domain to JPA entity
        AccountJpaEntity entity = new AccountJpaEntity(
            account.getAccountId(),
            account.getOwnerName(),
            account.getBalance()
        );
        //Save TO db
        AccountJpaEntity savedEntity = jpaRepository.save(entity);
        //Map JPA entity back to Domain
        return new Account(
            savedEntity.getAccountId(),
            savedEntity.getOwnerName(),
            savedEntity.getBalance()
        );
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        Optional<AccountJpaEntity> entityOpt = jpaRepository.findById(accountId);
        return entityOpt.map(entity -> new Account(
            entity.getAccountId(),
            entity.getOwnerName(),
            entity.getBalance()
        ));
    }

    @Override
    public List<Account> findAll(){
        //Fetch all entities from the database
        List<AccountJpaEntity> entities = jpaRepository.findAll();

        // Map each JPA entity to a Domain Account and collect into a list
        return entities.stream()
        .map(entity -> new Account(
            entity.getAccountId(),
            entity.getOwnerName(),
            entity.getBalance()
        ))
        .toList();
    }

   
    
}