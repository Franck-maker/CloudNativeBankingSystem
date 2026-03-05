package com.bank.banking_system.application.ports.out;
import java.util.*;

import com.bank.banking_system.domain.model.Account;
public interface AccountRepositoryPort {

    Account save(Account account); 
    //Forces the application layer to handle the "Not Found" scenario safely
    Optional<Account> findById(UUID accountId);
    
}
