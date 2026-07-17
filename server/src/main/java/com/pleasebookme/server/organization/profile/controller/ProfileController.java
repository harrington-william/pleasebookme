package com.pleasebookme.server.organization.profile.controller;

import com.pleasebookme.server.organization.profile.dto.ProfileRequest;
import com.pleasebookme.server.organization.profile.dto.ProfileResponse;
import com.pleasebookme.server.organization.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@Valid @RequestBody ProfileRequest request) {
        return ProfileResponse.from(profileService.createProfile(request));
    }

    @GetMapping("/{profileId}")
    public ProfileResponse getProfile(@PathVariable BigInteger profileId) {
        return ProfileResponse.from(profileService.getProfileById(profileId));
    }

    @GetMapping
    public List<ProfileResponse> getProfiles() {
        return profileService.getAllProfiles().stream()
            .map(ProfileResponse::from)
            .toList();
    }

    @PutMapping("/{profileId}")
    public ProfileResponse updateProfile(
        @PathVariable BigInteger profileId,
        @Valid @RequestBody ProfileRequest request
    ) {
        return ProfileResponse.from(profileService.updateProfile(profileId, request));
    }

    @DeleteMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable BigInteger profileId) {
        profileService.deleteProfile(profileId);
    }
}
