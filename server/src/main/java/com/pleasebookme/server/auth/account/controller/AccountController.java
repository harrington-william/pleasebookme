package com.pleasebookme.server.auth.account.controller;

import com.pleasebookme.server.auth.account.dto.AccountRequest;
import com.pleasebookme.server.auth.account.dto.AccountResponse;
import com.pleasebookme.server.auth.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody AccountRequest request) {
        return AccountResponse.from(accountService.createAccount(request));
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable BigInteger accountId) {
        return AccountResponse.from(accountService.getAccountById(accountId));
    }

    @GetMapping
    public List<AccountResponse> getAccounts() {
        return accountService.getAllAccounts().stream()
            .map(AccountResponse::from)
            .toList();
    }

    @PutMapping("/{accountId}")
    public AccountResponse updateAccount(
        @PathVariable BigInteger accountId,
        @Valid @RequestBody AccountRequest request
    ) {
        return AccountResponse.from(accountService.updateAccount(accountId, request));
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable BigInteger accountId) {
        accountService.deleteAccount(accountId);
    }
}
