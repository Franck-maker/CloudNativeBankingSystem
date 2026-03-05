package com.bank.banking_system.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID transactionId; 
    private final UUID accountId;
    private final BigDecimal amount; 
    private final LocalDateTime timestamp; 
    private final TransactionType type; 


    

    public Transaction(UUID transactionId, UUID accountId, BigDecimal amount, LocalDateTime timestamp, TransactionType type) {

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
    }

    
    // Getters
    public UUID getTransactionId(){
        return transactionId; 
    }
    public UUID getAccountId(){
        return accountId; 
    }
    public BigDecimal getAmount(){
        return amount; 
    }

    public LocalDateTime getTimestamp(){
        return timestamp; 
    }
    public TransactionType getType(){
        return type; 
    }
}
