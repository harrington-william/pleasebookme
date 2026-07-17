package com.pleasebookme.server.organization.profile.service;

import com.pleasebookme.server.organization.profile.dto.ProfileRequest;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;

import java.math.BigInteger;
import java.util.List;

public interface ProfileService {
    ProfileEntity createProfile(ProfileRequest request);

    ProfileEntity getProfileById(BigInteger profileId);

    List<ProfileEntity> getAllProfiles();

    ProfileEntity updateProfile(
        BigInteger profileId,
        ProfileRequest request
    );

    void deleteProfile(BigInteger profileId);
}
