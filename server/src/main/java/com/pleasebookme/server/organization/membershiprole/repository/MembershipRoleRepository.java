package com.pleasebookme.server.organization.membershiprole.repository;

import com.pleasebookme.server.organization.membershiprole.entity.MembershipRoleEntity;
import com.pleasebookme.server.organization.membershiprole.id.MembershipRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface MembershipRoleRepository extends JpaRepository<MembershipRoleEntity, MembershipRoleId> {
    List<MembershipRoleEntity> findByMembershipMembershipId(BigInteger membershipId);
}
