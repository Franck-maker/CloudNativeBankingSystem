package com.bank.banking_system.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.*; 
import java.math.*;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID accountId; 
    private String ownerName; 
    private BigDecimal balance;


    // Default constructor for JPA
    public AccountJpaEntity() {}

    public AccountJpaEntity(UUID accountId, String ownerName, BigDecimal balance) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    // Getters and Setters
    public UUID getAccountId(){
        return accountId; 
    }

    public String getOwnerName(){
        return ownerName; 
    }
    public BigDecimal getBalance(){
        return balance; 
    }

    public void setAccountId(UUID accountId){
        this.accountId = accountId; 
    }

    public void setOwnerName(String ownerName){
        this.ownerName = ownerName; 
    }
    public void setBalance(BigDecimal balance){
        this.balance = balance; 
    }
    
}
