package com.pleasebookme.server.organization.membershiprole.entity;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.membershiprole.id.MembershipRoleId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
    schema = "organization",
    name = "membership_roles"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipRoleEntity {
    @Builder.Default
    @EmbeddedId
    private MembershipRoleId membershipRoleId = new MembershipRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("membershipId")
    @JoinColumn(name = "membership_id")
    private MembershipEntity membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;
}
