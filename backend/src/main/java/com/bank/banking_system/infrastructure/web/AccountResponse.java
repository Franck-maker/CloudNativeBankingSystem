package com.bank.banking_system.infrastructure.web;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id, 
        String owner, 
        BigDecimal balance
    ){}
    

