package com.pleasebookme.server.organization.membershiprole.controller;

import com.pleasebookme.server.organization.membershiprole.dto.MembershipRoleRequest;
import com.pleasebookme.server.organization.membershiprole.dto.MembershipRoleResponse;
import com.pleasebookme.server.organization.membershiprole.service.MembershipRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/membership-roles")
@RequiredArgsConstructor
public class MembershipRoleController {
    private final MembershipRoleService membershipRoleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipRoleResponse createMembershipRole(@Valid @RequestBody MembershipRoleRequest request) {
        return MembershipRoleResponse.from(membershipRoleService.createMembershipRole(request));
    }

    @GetMapping("/{membershipId}/{roleId}")
    public MembershipRoleResponse getMembershipRole(
        @PathVariable BigInteger membershipId,
        @PathVariable BigInteger roleId
    ) {
        return MembershipRoleResponse.from(membershipRoleService.getMembershipRoleById(membershipId, roleId));
    }

    @GetMapping
    public List<MembershipRoleResponse> getMembershipRoles() {
        return membershipRoleService.getAllMembershipRoles().stream()
            .map(MembershipRoleResponse::from)
            .toList();
    }

    @DeleteMapping("/{membershipId}/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMembershipRole(
        @PathVariable BigInteger membershipId,
        @PathVariable BigInteger roleId
    ) {
        membershipRoleService.deleteMembershipRole(membershipId, roleId);
    }
}
