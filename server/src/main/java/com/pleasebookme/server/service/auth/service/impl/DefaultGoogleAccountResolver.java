package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.account.entity.AccountEntity;
import com.pleasebookme.server.auth.account.repository.AccountRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.service.auth.exception.GoogleAccountEmailNotVerifiedException;
import com.pleasebookme.server.service.auth.service.GoogleAccountResolver;
import com.pleasebookme.server.service.workspace.dto.WorkspaceProvisionRequest;
import com.pleasebookme.server.service.workspace.service.WorkspaceProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultGoogleAccountResolver implements GoogleAccountResolver {
    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final WorkspaceProvisioningService workspaceProvisioningService;

    @Override
    public UserEntity resolve(GoogleIdentity identity) {
        return accountRepository
            .findByProviderAndProviderAccountId(GOOGLE_PROVIDER, identity.sub())
            .map(AccountEntity::getUser)
            .orElseGet(() -> resolveOrProvisionUser(identity));
    }

    private UserEntity resolveOrProvisionUser(GoogleIdentity identity) {
        return userRepository.findByEmail(identity.email())
            .map(existingUser -> linkGoogleAccount(existingUser, identity))
            .orElseGet(() -> provisionAndLinkNewUser(identity));
    }

    private UserEntity linkGoogleAccount(
        UserEntity existingUser,
        GoogleIdentity identity
    ) {
        if (!identity.emailVerified()) {
            throw new GoogleAccountEmailNotVerifiedException(
                "Cannot link unverified Google account: " + identity.email()
            );
        }

        AccountEntity account = AccountEntity.builder()
            .user(existingUser)
            .type("oauth")
            .provider(GOOGLE_PROVIDER)
            .providerAccountId(identity.sub())
            .providerEmail(identity.email())
            .build();
        accountRepository.save(account);

        return existingUser;
    }

    private UserEntity provisionAndLinkNewUser(GoogleIdentity identity) {
        String username = resolveAvailableUsername(identity.email());

        UserEntity user = workspaceProvisioningService.provision(
            new WorkspaceProvisionRequest(
                username,
                identity.name() != null ? identity.name() : identity.email(),
                identity.email(),
                null,
                Locale.en,
                null
            )
        );

        AccountEntity account = AccountEntity.builder()
            .user(user)
            .type("oauth")
            .provider(GOOGLE_PROVIDER)
            .providerAccountId(identity.sub())
            .providerEmail(identity.email())
            .build();
        accountRepository.save(account);

        return user;
    }

    private String resolveAvailableUsername(String email) {
        String base = email
            .substring(0, Math.max(email.indexOf('@'), 0))
            .toLowerCase()
            .replaceAll("[^a-z0-9]", "");

        if (base.isBlank()) {
            base = "user";
        }

        if (!userRepository.existsByUsername(base)) {
            return base;
        }

        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
