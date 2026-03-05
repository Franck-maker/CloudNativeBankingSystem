package com.bank.banking_system.infrastructure.web;

import com.bank.banking_system.application.ports.in.AccountUseCase;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;

import com.bank.banking_system.domain.model.Account;

@RestController
@RequestMapping("api/v1/accounts")
@CrossOrigin(origins = "http://localhost:4200") // Allow requests from Angular app
public class AccountController {

    private final AccountUseCase accountUseCase;

    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {

        // We call the use case
        Account createdAccount = accountUseCase.createAccount(request.getOwner(), request.getInitialBalance());

        // Map the Domain Entity to a Response DTO
        AccountResponse response = new AccountResponse(
                createdAccount.getAccountId(),
                createdAccount.getOwnerName(),
                createdAccount.getBalance());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {

        Account account = accountUseCase.getAccount(id);

        AccountResponse response = new AccountResponse(
                account.getAccountId(),
                account.getOwnerName(),
                account.getBalance());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        try {
            accountUseCase.transferMoney(request.getSenderId(), request.getReceiverId(), request.getAmount());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {

            //Catch insufficient funds or invalid acccounts
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(){
        List<Account> accounts = accountUseCase.getAllAccounts(); 

        List<AccountResponse> responseList = accounts.stream()
                .map(account -> new AccountResponse(
                        account.getAccountId(),
                        account.getOwnerName(),
                        account.getBalance()))
                .toList();

        return ResponseEntity.ok(responseList);
    }

}
