package com.pleasebookme.server.security.identity.loader.user;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DefaultUserIdentityLoader implements UserIdentityLoader {
    private final UserRepository userRepository;

    @Override
    public AuthenticationAggregation loadByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + username
            ));

        return build(user);
    }

    @Override
    public AuthenticationAggregation loadByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + email
            ));

        return build(user);
    }

    @Override
    public AuthenticationAggregation loadByUid(UUID uid) {
        UserEntity user = userRepository.findByUserUid(uid)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + uid
            ));

        return build(user);
    }

    private AuthenticationAggregation build(UserEntity user) {
        Set<RoleEntity> roles = Set.copyOf(user.getRoles());

        Set<PermissionEntity> permissions = roles
            .stream()
            .flatMap(
                role -> role.getPermissions().stream()
            )
            .collect(Collectors.toUnmodifiableSet());

        return new AuthenticationAggregation(
            user,
            roles,
            permissions
        );
    }
}
