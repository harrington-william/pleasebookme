package com.pleasebookme.server.auth.account.service;

import com.pleasebookme.server.auth.account.dto.AccountRequest;
import com.pleasebookme.server.auth.account.entity.AccountEntity;

import java.math.BigInteger;
import java.util.List;

public interface AccountService {
    AccountEntity createAccount(AccountRequest request);

    AccountEntity getAccountById(BigInteger accountId);

    List<AccountEntity> getAllAccounts();

    AccountEntity updateAccount(BigInteger accountId, AccountRequest request);

    void deleteAccount(BigInteger accountId);
}
