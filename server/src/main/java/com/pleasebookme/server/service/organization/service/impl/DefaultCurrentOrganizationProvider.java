package com.pleasebookme.server.service.organization.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.exception.ProfileNotFoundException;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.security.authorization.membership.MembershipResolver;
import com.pleasebookme.server.security.authorization.membership.MembershipSnapshot;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.exception.AmbiguousOrganizationContextException;
import com.pleasebookme.server.service.organization.exception.NoOrganizationMembershipException;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultCurrentOrganizationProvider implements CurrentOrganizationProvider {
    private final CurrentPrincipalProvider currentPrincipalProvider;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final ProfileRepository profileRepository;
    private final MembershipResolver membershipResolver;

    @Override
    @Transactional(readOnly = true)
    public OrganizationContext requireCurrent() {
        BigInteger userId = currentPrincipalProvider.requireUser().userId();

        List<MembershipEntity> memberships =
            membershipRepository.findAllByUserUserIdAndAccepted(userId, true);

        if (memberships.isEmpty()) {
            throw new NoOrganizationMembershipException(
                "User " + userId + " has no accepted organization membership."
            );
        }

        if (memberships.size() > 1) {
            throw new AmbiguousOrganizationContextException(
                "User " + userId + " has accepted memberships in " + memberships.size()
                    + " organizations; this request must name the organization it concerns."
            );
        }

        return toContext(userId, memberships.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationContext require(BigInteger organizationId) {
        UserPrincipal principal = currentPrincipalProvider.requireUser();

        if (organizationId == null) {
            throw new NoOrganizationMembershipException(
                "User " + principal.userId() + " supplied no organization to act in."
            );
        }

        MembershipSnapshot snapshot = membershipResolver
            .resolve(principal.subject(), organizationId)
            .orElseThrow(() -> new NoOrganizationMembershipException(
                "User " + principal.userId() + " has no accepted membership in organization " + organizationId + "."
            ));

        MembershipEntity membership = membershipRepository.findById(snapshot.membershipId())
            .orElseThrow(() -> new NoOrganizationMembershipException(
                "User " + principal.userId() + " has no accepted membership in organization " + organizationId + "."
            ));

        return toContext(principal.userId(), membership);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileEntity requireProfile(OrganizationContext context) {
        return profileRepository
            .findByUserUserIdAndOrganizationOrganizationId(context.userId(), context.organizationId())
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found for user " + context.userId()
                    + " in organization " + context.organizationId()
            ));
    }

    private OrganizationContext toContext(
        BigInteger userId,
        MembershipEntity membership
    ) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        return new OrganizationContext(user, membership, membership.getOrganization());
    }
}
