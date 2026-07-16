package com.pleasebookme.server.tenant.domain.entity;

import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    schema = "tenant",
    name = "tenant_domains"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger tenantDomainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "domain", nullable = false, unique = true)
    private String domain;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = true;

    @Column(name = "verification_token", nullable = false)
    private String verificationToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
