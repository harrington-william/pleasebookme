package com.pleasebookme.server.tenant.tenants.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.tenant.ecosystem.entity.EcosystemEntity;
import com.pleasebookme.server.tenant.enums.TenantRegion;
import com.pleasebookme.server.tenant.enums.TenantStatus;
import com.pleasebookme.server.tenant.plan.entity.TenantPlanEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    schema = "tenant",
    name = "tenants"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger tenantId;

    @UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID tenantUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity ownerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecosystem_id", nullable = false)
    private EcosystemEntity ecosystem;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private TenantStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private TenantPlanEntity plan;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "region", nullable = false)
    private TenantRegion region;

    @Builder.Default
    @Column(name = "default_timezone", nullable = false, length = 100)
    private String defaultTimezone = "Australia/Sydney";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "default_locale", nullable = false)
    private Locale defaultLocale = Locale.en;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "max_services", nullable = false)
    private Integer maxServices;

    @Column(name = "max_widgets", nullable = false)
    private Integer maxWidgets;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings")
    private JsonNode settings;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
