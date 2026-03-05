package com.bank.banking_system.infrastructure.web;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAccountRequest {

    @NotBlank(message = "Owner name is required")
    private String owner;

    @NotNull(message = "Initial balance cannot be null")
    @Min(value = 0, message = "Initial balance must be non-negative")
    private BigDecimal initialBalance;

    // Default constructor for Jackson
    public CreateAccountRequest() {
    }

    public CreateAccountRequest(String owner, BigDecimal initialBalance) {
        this.owner = owner;
        this.initialBalance = initialBalance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
