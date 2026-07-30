package com.pleasebookme.server.organization.membership.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.organization.membership.dto.MembershipRequest;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membership.exception.DuplicateMembershipException;
import com.pleasebookme.server.organization.membership.exception.MembershipNotFoundException;
import com.pleasebookme.server.organization.membership.repository.MembershipRepository;
import com.pleasebookme.server.organization.membership.service.MembershipService;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {
    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override
    public MembershipEntity createMembership(MembershipRequest request) {
        if (membershipRepository.existsByUserUserIdAndOrganizationOrganizationId(
            request.userId(),
            request.organizationId()
        )) {
            throw new DuplicateMembershipException(
                "User " + request.userId() + " is already a member of organization " + request.organizationId()
            );
        }

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        MembershipEntity.MembershipEntityBuilder builder = MembershipEntity.builder()
            .organization(organization)
            .user(user);

        if (request.accepted() != null) {
            builder.accepted(request.accepted());
        }

        return membershipRepository.save(builder.build());
    }

    @Override
    public MembershipEntity getMembershipById(BigInteger membershipId) {
        return membershipRepository.findById(membershipId)
            .orElseThrow(() -> new MembershipNotFoundException("Membership not found: " + membershipId));
    }

    @Override
    public List<MembershipEntity> getAllMemberships() {
        return membershipRepository.findAll();
    }

    @Override
    public MembershipEntity updateMembership(
        BigInteger membershipId,
        MembershipRequest request
    ) {
        MembershipEntity membership = getMembershipById(membershipId);

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        membership.setOrganization(organization);
        membership.setUser(user);

        if (request.accepted() != null) {
            membership.setAccepted(request.accepted());
        }

        return membershipRepository.save(membership);
    }

    @Override
    public void deleteMembership(BigInteger membershipId) {
        membershipRepository.delete(getMembershipById(membershipId));
    }
}
