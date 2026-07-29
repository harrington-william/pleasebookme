package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;
import com.pleasebookme.server.auth.password.repository.UserPasswordRepository;
import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.enums.RefreshOwner;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.role.repository.RoleRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserEmailAlreadyExistException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.exception.UserPhoneNumberAlreadyExistException;
import com.pleasebookme.server.auth.user.exception.UsernameAlreadyExistException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;
import com.pleasebookme.server.auth.userrole.repository.UserRoleRepository;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.global.enums.Theme;
import com.pleasebookme.server.global.enums.WeekStart;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.security.identity.adapter.PrincipalUserDetails;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.loader.widget.WidgetIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.security.token.refresh.TokenRefresher;
import com.pleasebookme.server.service.auth.dto.*;
import com.pleasebookme.server.service.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtEngine jwtEngine;
    private final TokenRefresher tokenRefresher;

    private final UserIdentityLoader userIdentityLoader;
    private final UserPrincipalMapper userPrincipalMapper;

    private final WidgetIdentityLoader widgetIdentityLoader;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

        PrincipalUserDetails userDetails = (PrincipalUserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new UserNotFoundException("User not found");
        }

        UserPrincipal principal = userDetails.getUserPrincipal();

        String accessToken = jwtEngine.issueAccessToken(principal);
        String refreshToken = jwtEngine.issueRefreshToken(principal);

        UserEntity user = userRepository.findByUsername(principal.username())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + principal.username()
            ));

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(refreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(null)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistException(
                "Username already exists: " + request.username()
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserEmailAlreadyExistException(
                "Email already exists: " + request.email()
            );
        }

        if (request.phone() != null && userRepository.existsByPhone(request.phone())) {
            throw new UserPhoneNumberAlreadyExistException(
                "Phone number already exists: " + request.phone()
            );
        }

        Instant now = Instant.now();
        String timezone = request.timezone() != null
            ? request.timezone()
            : "Australia/Sydney";

        UserEntity user = UserEntity.builder()
            .userUid(UUID.randomUUID())
            .username(request.username())
            .name(request.name())
            .email(request.email())
            .phone(
                request.phone() != null
                    ? request.phone()
                    : null
            )
            .locale(
                request.locale() != null
                    ? request.locale()
                    : Locale.en
            )
            .timezone(timezone)
            .theme(Theme.DARK)
            .weekStart(WeekStart.MONDAY)
            .accountStatus(AccountStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();
        userRepository.save(user);

        UserPasswordEntity userPasswordEntity = UserPasswordEntity.builder()
            .user(user)
            .raw(request.password())
            .hash(passwordEncoder.encode(request.password()))
            .createdAt(now)
            .updatedAt(now)
            .build();
        userPasswordRepository.save(userPasswordEntity);

        // Assign USER role
        RoleEntity userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RoleNotFoundException(
                "Role not found: USER"
            ));

        UserRoleEntity userRoleEntity = UserRoleEntity.builder()
            .user(user)
            .role(userRole)
            .build();
        userRoleRepository.save(userRoleEntity);

        String organizationName = request.name() + "'s Organization";
        String organizationSlug = request.name()
            .toLowerCase()
            .replace(" ", "") +
            "-organization";

        if (organizationRepository.existsBySlug(organizationSlug)) {
            organizationSlug = user.getUserUid().toString();
        }

        OrganizationEntity organization = OrganizationEntity.builder()
            .name(organizationName)
            .slug(organizationSlug)
            .isPrivate(true)
            .timezone(user.getTimezone())
            .build();
        organizationRepository.save(organization);

        MembershipEntity membership = MembershipEntity.builder()
            .organization(organization)
            .user(user)
            .accepted(true)
            .build();
        membershipRepository.save(membership);

        ProfileEntity profile = ProfileEntity.builder()
            .user(user)
            .organization(organization)
            .username(request.username())
            .build();
        profileRepository.save(profile);

        // Create principal
        AuthenticationAggregation aggregation = userIdentityLoader.loadByUsername(user.getUsername());
        UserPrincipal principal = userPrincipalMapper.map(aggregation);

        // Issue tokens
        String accessToken = jwtEngine.issueAccessToken(principal);
        String refreshToken = jwtEngine.issueRefreshToken(principal);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(refreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(null)
            .createdAt(now)
            .expiresAt(now.plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RefreshResponse refreshToken(String token) {
        return tokenRefresher.refresh(token);
    }

    @Override
    @Transactional
    public WidgetBootstrapResponse bootstrapWidget(WidgetBootstrapRequest request) {
        WidgetPrincipal principal = widgetIdentityLoader.loadByPublicKey(
            request.publicKey(),
            request.secretKey(),
            request.origin()
        );

        String accessToken = jwtEngine.issueAccessToken(principal);

        return new WidgetBootstrapResponse(accessToken);
    }
}
