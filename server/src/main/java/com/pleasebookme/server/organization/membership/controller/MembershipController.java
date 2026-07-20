package com.pleasebookme.server.organization.membership.controller;

import com.pleasebookme.server.organization.membership.dto.MembershipRequest;
import com.pleasebookme.server.organization.membership.dto.MembershipResponse;
import com.pleasebookme.server.organization.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse createMembership(@Valid @RequestBody MembershipRequest request) {
        return MembershipResponse.from(membershipService.createMembership(request));
    }

    @GetMapping("/{membershipId}")
    public MembershipResponse getMembership(@PathVariable BigInteger membershipId) {
        return MembershipResponse.from(membershipService.getMembershipById(membershipId));
    }

    @GetMapping
    public List<MembershipResponse> getMemberships() {
        return membershipService.getAllMemberships().stream()
            .map(MembershipResponse::from)
            .toList();
    }

    @PutMapping("/{membershipId}")
    public MembershipResponse updateMembership(
        @PathVariable BigInteger membershipId,
        @Valid @RequestBody MembershipRequest request
    ) {
        return MembershipResponse.from(membershipService.updateMembership(membershipId, request));
    }

    @DeleteMapping("/{membershipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMembership(@PathVariable BigInteger membershipId) {
        membershipService.deleteMembership(membershipId);
    }
}
