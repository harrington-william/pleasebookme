package com.pleasebookme.server.organization.membershiprole.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class MembershipRoleId implements Serializable {
    @Column(name = "membership_id")
    private BigInteger membershipId;

    @Column(name = "role_id")
    private BigInteger roleId;

    public MembershipRoleId() {}

    public MembershipRoleId(
        BigInteger membershipId,
        BigInteger roleId
    ) {
        this.membershipId = membershipId;
        this.roleId = roleId;
    }

    public BigInteger getMembershipId() { return membershipId; }
    public void setMembershipId(BigInteger membershipId) { this.membershipId = membershipId; }

    public BigInteger getRoleId() { return roleId; }
    public void setRoleId(BigInteger roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembershipRoleId that = (MembershipRoleId) o;
        return Objects.equals(membershipId, that.membershipId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(membershipId, roleId);
    }
}
