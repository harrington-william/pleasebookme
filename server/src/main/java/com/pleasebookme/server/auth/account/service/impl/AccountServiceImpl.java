package com.pleasebookme.server.auth.account.service.impl;

import com.pleasebookme.server.auth.account.dto.AccountRequest;
import com.pleasebookme.server.auth.account.entity.AccountEntity;
import com.pleasebookme.server.auth.account.exception.AccountNotFoundException;
import com.pleasebookme.server.auth.account.exception.DuplicateAccountException;
import com.pleasebookme.server.auth.account.repository.AccountRepository;
import com.pleasebookme.server.auth.account.service.AccountService;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public AccountEntity createAccount(AccountRequest request) {
        if (accountRepository.existsByProviderAndProviderAccountId(request.provider(), request.providerAccountId())) {
            throw new DuplicateAccountException(
                "Account already linked for provider " + request.provider() + " and provider account " + request.providerAccountId()
            );
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        AccountEntity.AccountEntityBuilder account = AccountEntity.builder()
            .user(user)
            .provider(request.provider())
            .providerAccountId(request.providerAccountId())
            .providerEmail(request.providerEmail());

        if (request.type() != null) account.type(request.type());

        return accountRepository.save(account.build());
    }

    @Override
    public AccountEntity getAccountById(BigInteger accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(
                "Account not found: " + accountId
            ));
    }

    @Override
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public AccountEntity updateAccount(
        BigInteger accountId,
        AccountRequest request
    ) {
        AccountEntity account = getAccountById(accountId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        account.setUser(user);
        account.setProvider(request.provider());
        account.setProviderAccountId(request.providerAccountId());
        account.setProviderEmail(request.providerEmail());

        if (request.type() != null) account.setType(request.type());

        return accountRepository.save(account);
    }

    @Override
    public void deleteAccount(BigInteger accountId) {
        accountRepository.delete(getAccountById(accountId));
    }
}
