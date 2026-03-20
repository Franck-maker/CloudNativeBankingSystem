package com.bank.banking_system.application.service;
import com.bank.banking_system.application.ports.in.*;
import com.bank.banking_system.application.ports.out.*;
import java.util.*;
import org.springframework.stereotype.Service;
import com.bank.banking_system.domain.model.Account;
import com.bank.banking_system.infrastructure.notification.NotificationGrpcClient;

import java.math.*;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AccountService implements AccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final NotificationGrpcClient notificationClient;

    public AccountService(AccountRepositoryPort accountRepositoryPort, NotificationGrpcClient notificationClient){
        this.accountRepository = accountRepositoryPort; 
        this.notificationClient = notificationClient;
    }

    @Override
    public Account createAccount(String owner, BigDecimal initialBalance) {
        //Create the domain object
        Account newAccount = new Account(UUID.randomUUID(), owner, initialBalance);
        //saving it via the output port and return it 
        return accountRepository.save(newAccount);
    }

    @Override
    public Account getAccount(UUID accountId){
        //Find the account by ID, if not found throw an exception
        return accountRepository.findById(accountId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: "+accountId)); 
    }

    @Override
    public List<Account> getAllAccounts(){
        return accountRepository.findAll();
    }
    
    @Override
    //Ensures that the transfer operation is atomic and consistent
    // Basically ensure that money is never lost if an error occurs during the transfer process
    @Transactional 
    public void transferMoney(UUID senderId, UUID receiverId, BigDecimal amount){
        //Find both Sender and receiver by their Ids
        Account sender = accountRepository.findById(senderId)
        .orElseThrow(() -> new IllegalArgumentException("Sender not found with ID: "+senderId));

        Account receiver = accountRepository.findById(receiverId)
        .orElseThrow(() -> new IllegalArgumentException("Receiver not found with ID: "+receiverId));

        //withdraw from sender and deposit to receiver
        sender.withdraw(amount);
        receiver.deposit(amount);

        //Save the updated accounts
        accountRepository.save(sender); 
        accountRepository.save(receiver); 
    }

    @Override
    public void notifyTransfer(UUID senderId, BigDecimal amount) {
        // THE gRPC TRIGGER - called AFTER transaction commits so it doesn't block the DB transaction
        String message = String.format("Successful transfer of $%.2f. Everything is working just fine", amount);
        
        notificationClient.sendTransferAlert(
                senderId.toString(),
                amount.doubleValue(),
                message
        );
    }
}
