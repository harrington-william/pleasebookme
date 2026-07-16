package com.pleasebookme.server.auth.userrole.entity;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.userrole.id.UserRoleId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
    schema = "auth",
    name = "user_roles"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleEntity {
    @Builder.Default
    @EmbeddedId
    private UserRoleId userRoleId = new UserRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;
}
