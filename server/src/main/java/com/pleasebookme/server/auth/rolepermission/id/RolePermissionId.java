package com.pleasebookme.server.auth.rolepermission.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class RolePermissionId implements Serializable {
    @Column(name = "role_id")
    private BigInteger roleId;

    @Column(name = "permission_id")
    private BigInteger permissionId;

    public RolePermissionId() {}

    public RolePermissionId(
        BigInteger roleId,
        BigInteger permissionId
    ) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public BigInteger getRoleId() { return roleId; }
    public void setRoleId(BigInteger roleId) { this.roleId = roleId; }

    public BigInteger getPermissionId() { return permissionId; }
    public void setPermissionId(BigInteger permissionId) { this.permissionId = permissionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermissionId that = (RolePermissionId) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
