package com.bank.banking_system.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The Domain Entity : Represents a bank account in the banking system.
 * It contains the fundamental business rules
 * it's an aggregate root that encapsulates the account's state and behavior,
 * ensuring consistency and integrity of the account data.
 */
public class Account {
    private final UUID accountId;
    private final String ownerName; 
    private BigDecimal balance;

    //we don't use Autowire here
    // This is pure java to keep the core(domain) independent
    public Account(UUID accountId, String ownerName, BigDecimal initialBalance){
        if(initialBalance.compareTo(BigDecimal.ZERO) <0){
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.balance = initialBalance;
    }

    //Business logic : Encapsulation
    //we don't provide a setBalance() because a balance
    //should only be modified through deposit and withdrawal methods
    public void deposit(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("the amount must be greater than zero"); 
        }
        this.balance = this.balance.add(amount); 
    }

    public void withdraw(BigDecimal amount){
        if(amount.compareTo(this.balance) > 0){
            throw new IllegalArgumentException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount); 
    }

    //Getters
    public UUID getAccountId(){
        return accountId; 
    }

    public String getOwnerName(){
        return ownerName; 
    }
    public BigDecimal getBalance(){
        return balance; 
    }
    
}
