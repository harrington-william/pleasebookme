package com.pleasebookme.server.organization.membershiprole.repository;

import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.id.MembershipRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRoleRepository extends JpaRepository<MembershipRoleEntity, MembershipRoleId> {
}
