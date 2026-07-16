package com.pleasebookme.server.auth.userrole.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class UserRoleId implements Serializable {
    @Column(name = "user_id")
    private BigInteger userId;

    @Column(name = "role_id")
    private BigInteger roleId;

    public UserRoleId() {}

    public UserRoleId(
        BigInteger userId,
        BigInteger roleId
    ) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public BigInteger getUserId() { return userId; }
    public void setUserId(BigInteger userId) { this.userId = userId; }

    public BigInteger getRoleId() { return roleId; }
    public void setRoleId(BigInteger roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
