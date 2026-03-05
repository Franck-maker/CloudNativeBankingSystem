package com.bank.banking_system.application.ports.in;

import java.math.BigDecimal;
import java.util.*;

import com.bank.banking_system.domain.model.Account; 

/**
 * The AccountUseCase interface defines the operations related to bank accounts in the banking system.
 * Tells the Controller what it can do
 */

public interface AccountUseCase {
    //Returns the created account so the caller knows the new UUID
    Account createAccount (String owner, BigDecimal initialBalance); 
    // Returns the requested Account
    Account getAccount(UUID accountId); 
    //Performs a transfer between two accounts, ensuring atomicity and consistency
    void transferMoney(UUID senderId, UUID receiver, BigDecimal amount); 
    // Returns a list of all accounts in the system
    List<Account> getAllAccounts();
}
