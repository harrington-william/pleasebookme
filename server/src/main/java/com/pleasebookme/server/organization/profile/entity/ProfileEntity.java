package com.pleasebookme.server.organization.profile.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    schema = "organization",
    name = "profiles"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger profileId;

    @UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID profileUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
