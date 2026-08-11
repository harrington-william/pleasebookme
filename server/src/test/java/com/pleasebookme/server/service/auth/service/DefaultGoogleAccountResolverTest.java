package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.account.entity.AccountEntity;
import com.pleasebookme.server.auth.account.repository.AccountRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import com.pleasebookme.server.service.auth.exception.GoogleAccountEmailNotVerifiedException;
import com.pleasebookme.server.service.auth.service.impl.DefaultGoogleAccountResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Both Google entry points route through this component, so the ordering of the
// three resolution steps - and the emailVerified gate in step two - is asserted
// here rather than once per flow.
@ExtendWith(MockitoExtension.class)
class DefaultGoogleAccountResolverTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserProvisioningService userProvisioningService;

    private DefaultGoogleAccountResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DefaultGoogleAccountResolver(
            accountRepository,
            userRepository,
            userProvisioningService
        );
    }

    @Test
    void alreadyLinkedAccount_returnsTheUserWithoutWritingAnything() {
        GoogleIdentity identity =
            new GoogleIdentity("sub-1", "user@example.com", true, "Jane Doe", null);

        UserEntity existingUser = UserEntity.builder()
            .username("jane").email("user@example.com").build();
        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-1"))
            .thenReturn(Optional.of(AccountEntity.builder().user(existingUser).build()));

        assertThat(resolver.resolve(identity)).isEqualTo(existingUser);

        verify(userRepository, never()).findByEmail(anyString());
        verify(userProvisioningService, never()).provisionUser(any(), any(), any(), any(), any(), any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void noLinkButVerifiedEmailMatches_linksExistingUserWithoutProvisioning() {
        GoogleIdentity identity =
            new GoogleIdentity("sub-2", "user@example.com", true, "Jane Doe", null);

        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-2"))
            .thenReturn(Optional.empty());

        UserEntity existingUser = UserEntity.builder()
            .username("jane").email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThat(resolver.resolve(identity)).isEqualTo(existingUser);

        ArgumentCaptor<AccountEntity> accountCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUser()).isEqualTo(existingUser);
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo("GOOGLE");
        assertThat(accountCaptor.getValue().getProviderAccountId()).isEqualTo("sub-2");

        verify(userProvisioningService, never()).provisionUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    void noLinkAndUnverifiedEmailMatch_rejectsWithoutLinkingOrProvisioning() {
        // The account-takeover control: without it, anyone able to create a
        // Google account bearing a victim's address could claim their account.
        GoogleIdentity identity =
            new GoogleIdentity("sub-3", "user@example.com", false, "Jane Doe", null);

        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-3"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com"))
            .thenReturn(Optional.of(UserEntity.builder().username("jane").build()));

        assertThatThrownBy(() -> resolver.resolve(identity))
            .isInstanceOf(GoogleAccountEmailNotVerifiedException.class);

        verify(accountRepository, never()).save(any());
        verify(userProvisioningService, never()).provisionUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    void unverifiedEmailWithNoMatchingUser_stillProvisions() {
        // The gate guards linking to somebody else's existing account, not
        // creating a fresh one - there is nothing to take over here.
        GoogleIdentity identity =
            new GoogleIdentity("sub-3b", "nobody@example.com", false, "Jane Doe", null);

        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-3b"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("nobody")).thenReturn(false);

        UserEntity newUser = UserEntity.builder().username("nobody").build();
        when(userProvisioningService.provisionUser(
            eq("nobody"), anyString(), anyString(), isNull(), isNull(), isNull()
        )).thenReturn(newUser);

        assertThat(resolver.resolve(identity)).isEqualTo(newUser);
    }

    @Test
    void noExistingUser_provisionsNewUserAndLinksAccount() {
        GoogleIdentity identity =
            new GoogleIdentity("sub-4", "jane.doe@example.com", true, "Jane Doe", null);

        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-4"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);

        UserEntity newUser = UserEntity.builder()
            .username("janedoe").email("jane.doe@example.com").build();
        when(userProvisioningService.provisionUser(
            eq("janedoe"), eq("Jane Doe"), eq("jane.doe@example.com"), isNull(), isNull(), isNull()
        )).thenReturn(newUser);

        assertThat(resolver.resolve(identity)).isEqualTo(newUser);

        verify(userProvisioningService)
            .provisionUser("janedoe", "Jane Doe", "jane.doe@example.com", null, null, null);

        ArgumentCaptor<AccountEntity> accountCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUser()).isEqualTo(newUser);
    }

    @Test
    void derivedUsernameAlreadyTaken_fallsBackToSuffixedUsername() {
        GoogleIdentity identity =
            new GoogleIdentity("sub-5", "jane.doe@example.com", true, "Jane Doe", null);

        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-5"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        UserEntity newUser = UserEntity.builder().username("janedoe-fallback").build();
        when(userProvisioningService.provisionUser(
            argThatStartsWith("janedoe-"), eq("Jane Doe"), eq("jane.doe@example.com"),
            isNull(), isNull(), isNull()
        )).thenReturn(newUser);

        resolver.resolve(identity);

        verify(userProvisioningService).provisionUser(
            argThatStartsWith("janedoe-"), eq("Jane Doe"), eq("jane.doe@example.com"),
            isNull(), isNull(), isNull()
        );
    }

    @Test
    void identityWithNoName_fallsBackToTheEmailAsDisplayName() {
        GoogleIdentity identity =
            new GoogleIdentity("sub-6", "jane.doe@example.com", true, null, null);

        when(accountRepository.findByProviderAndProviderAccountId("GOOGLE", "sub-6"))
            .thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userProvisioningService.provisionUser(
            anyString(), anyString(), anyString(), isNull(), isNull(), isNull()
        )).thenReturn(UserEntity.builder().username("janedoe").build());

        resolver.resolve(identity);

        verify(userProvisioningService).provisionUser(
            "janedoe", "jane.doe@example.com", "jane.doe@example.com", null, null, null
        );
    }

    private static String argThatStartsWith(String prefix) {
        return org.mockito.ArgumentMatchers.argThat(value -> value != null && value.startsWith(prefix));
    }
}
