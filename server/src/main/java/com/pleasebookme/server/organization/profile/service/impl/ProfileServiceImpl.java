package com.pleasebookme.server.organization.profile.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.profile.dto.ProfileRequest;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.organization.profile.exception.DuplicateProfileException;
import com.pleasebookme.server.organization.profile.exception.ProfileNotFoundException;
import com.pleasebookme.server.organization.profile.repository.ProfileRepository;
import com.pleasebookme.server.organization.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public ProfileEntity createProfile(ProfileRequest request) {
        if (profileRepository.existsByUserUserIdAndOrganizationOrganizationId(request.userId(), request.organizationId())) {
            throw new DuplicateProfileException(
                "Profile already exists for user " + request.userId() + " in organization " + request.organizationId()
            );
        }

        if (profileRepository.existsByUsernameAndOrganizationOrganizationId(request.username(), request.organizationId())) {
            throw new DuplicateProfileException("Username already exists in organization: " + request.username());
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        ProfileEntity profile = ProfileEntity.builder()
            .user(user)
            .organization(organization)
            .username(request.username())
            .build();

        return profileRepository.save(profile);
    }

    @Override
    public ProfileEntity getProfileById(BigInteger profileId) {
        return profileRepository.findById(profileId)
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found: " + profileId
            ));
    }

    @Override
    public List<ProfileEntity> getAllProfiles() {
        return profileRepository.findAll();
    }

    @Override
    public ProfileEntity updateProfile(
        BigInteger profileId,
        ProfileRequest request
    ) {
        ProfileEntity profile = getProfileById(profileId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        profile.setUser(user);
        profile.setOrganization(organization);
        profile.setUsername(request.username());

        return profileRepository.save(profile);
    }

    @Override
    public void deleteProfile(BigInteger profileId) {
        profileRepository.delete(getProfileById(profileId));
    }
}
