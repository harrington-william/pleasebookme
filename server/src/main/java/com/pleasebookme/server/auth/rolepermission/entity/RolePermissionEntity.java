package com.pleasebookme.server.auth.rolepermission.entity;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.rolepermission.id.RolePermissionId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    schema = "auth",
    name = "role_permissions"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionEntity {
    @Builder.Default
    @EmbeddedId
    private RolePermissionId rolePermissionId = new RolePermissionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private PermissionEntity permission;
}
