package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.account.entity.AccountEntity;
import com.pleasebookme.server.auth.account.repository.AccountRepository;
import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.security.oauth.google.verifier.GoogleTokenVerifier;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.exception.GoogleAccountEmailNotVerifiedException;
import com.pleasebookme.server.service.auth.service.impl.DefaultGoogleSignInService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGoogleSignInServiceTest {

    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserProvisioningService userProvisioningService;
    @Mock private UserIdentityLoader userIdentityLoader;
    @Mock private UserPrincipalMapper userPrincipalMapper;
    @Mock private JwtEngine jwtEngine;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    private DefaultGoogleSignInService googleSignInService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            "pleasebookme",
            "pleasebookme-api",
            "HS256",
            "test-secret",
            Duration.ofMinutes(15),
            Duration.ofDays(30)
        );

        googleSignInService = new DefaultGoogleSignInService(
            googleTokenVerifier,
            accountRepository,
            userRepository,
            userProvisioningService,
            userIdentityLoader,
            userPrincipalMapper,
            jwtEngine,
            jwtProperties,
            refreshTokenRepository
        );
    }

    private void stubTokenIssuance(UserEntity user) {
        AuthenticationAggregation aggregation = new AuthenticationAggregation(
            user, null, null, null, null, Set.of(), Set.of()
        );
        when(userIdentityLoader.loadByUsername(user.getUsername())).thenReturn(aggregation);

        UserPrincipal principal = new UserPrincipal(
            AuthenticatedActorType.USER,
            UUID.randomUUID(), null, null, null, null,
            user.getUsername(), user.getEmail(), user.getName(), null,
            "Australia/Sydney", AccountStatus.ACTIVE, Set.of(), Set.of(), Map.of()
        );
        when(userPrincipalMapper.map(aggregation)).thenReturn(principal);

        when(jwtEngine.issueAccessToken(principal)).thenReturn("access-token");
        when(jwtEngine.issueRefreshToken(principal)).thenReturn("refresh-token");
    }

    @Test
    void signInWithGoogle_alreadyLinkedAccount_returnsTokensWithoutCreatingAnything() {
        GoogleIdentity identity = new GoogleIdentity("sub-1", "user@example.com", true, "Jane Doe", null);
        when(googleTokenVerifier.verify("raw-token")).thenReturn(identity);

        UserEntity existingUser = UserEntity.builder().username("jane").email("user@example.com").build();
        AccountEntity linkedAccount = AccountEntity.builder().user(existingUser).build();
        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-1"))
            .thenReturn(Optional.of(linkedAccount));

        stubTokenIssuance(existingUser);

        LoginResponse response = googleSignInService.signIn("raw-token");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userProvisioningService, never()).provisionUser(any(), any(), any(), any(), any(), any());
        verify(accountRepository, never()).save(any());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void signInWithGoogle_noLinkButVerifiedEmailMatches_linksExistingUserWithoutProvisioning() {
        GoogleIdentity identity = new GoogleIdentity("sub-2", "user@example.com", true, "Jane Doe", null);
        when(googleTokenVerifier.verify("raw-token")).thenReturn(identity);
        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-2"))
            .thenReturn(Optional.empty());

        UserEntity existingUser = UserEntity.builder().username("jane").email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        stubTokenIssuance(existingUser);

        LoginResponse response = googleSignInService.signIn("raw-token");

        assertThat(response.accessToken()).isEqualTo("access-token");

        ArgumentCaptor<AccountEntity> accountCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUser()).isEqualTo(existingUser);
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo("GOOGLE");
        assertThat(accountCaptor.getValue().getProviderAccountId()).isEqualTo("sub-2");

        verify(userProvisioningService, never()).provisionUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    void signInWithGoogle_noLinkAndUnverifiedEmailMatch_rejectsWithoutLinkingOrProvisioning() {
        GoogleIdentity identity = new GoogleIdentity("sub-3", "user@example.com", false, "Jane Doe", null);
        when(googleTokenVerifier.verify("raw-token")).thenReturn(identity);
        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-3"))
            .thenReturn(Optional.empty());

        UserEntity existingUser = UserEntity.builder().username("jane").email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> googleSignInService.signIn("raw-token"))
            .isInstanceOf(GoogleAccountEmailNotVerifiedException.class);

        verify(accountRepository, never()).save(any());
        verify(userProvisioningService, never()).provisionUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    void signInWithGoogle_noExistingUser_provisionsNewUserAndLinksAccount() {
        GoogleIdentity identity = new GoogleIdentity("sub-4", "jane.doe@example.com", true, "Jane Doe", null);
        when(googleTokenVerifier.verify("raw-token")).thenReturn(identity);
        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-4"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);

        UserEntity newUser = UserEntity.builder().username("janedoe").email("jane.doe@example.com").build();
        when(userProvisioningService.provisionUser(
            eq("janedoe"), eq("Jane Doe"), eq("jane.doe@example.com"), isNull(), isNull(), isNull()
        )).thenReturn(newUser);

        stubTokenIssuance(newUser);

        LoginResponse response = googleSignInService.signIn("raw-token");

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(userProvisioningService).provisionUser("janedoe", "Jane Doe", "jane.doe@example.com", null, null, null);

        ArgumentCaptor<AccountEntity> accountCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUser()).isEqualTo(newUser);
    }

    @Test
    void signInWithGoogle_derivedUsernameAlreadyTaken_fallsBackToSuffixedUsername() {
        GoogleIdentity identity = new GoogleIdentity("sub-5", "jane.doe@example.com", true, "Jane Doe", null);
        when(googleTokenVerifier.verify("raw-token")).thenReturn(identity);
        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-5"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        UserEntity newUser = UserEntity.builder().username("janedoe-fallback").email("jane.doe@example.com").build();
        when(userProvisioningService.provisionUser(
            argThatStartsWith("janedoe-"), eq("Jane Doe"), eq("jane.doe@example.com"), isNull(), isNull(), isNull()
        )).thenReturn(newUser);

        stubTokenIssuance(newUser);

        googleSignInService.signIn("raw-token");

        verify(userProvisioningService).provisionUser(
            argThatStartsWith("janedoe-"), eq("Jane Doe"), eq("jane.doe@example.com"), isNull(), isNull(), isNull()
        );
    }

    private static String argThatStartsWith(String prefix) {
        return org.mockito.ArgumentMatchers.argThat(value -> value != null && value.startsWith(prefix));
    }
}
