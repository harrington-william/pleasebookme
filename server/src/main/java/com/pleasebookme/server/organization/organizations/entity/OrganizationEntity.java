package com.pleasebookme.server.organization.organizations.entity;

import com.pleasebookme.server.global.enums.WeekStart;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(
    schema = "organization",
    name = "organizations"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "bio")
    private String bio;

    @Builder.Default
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone = "Australia/Sydney";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "week_start", nullable = false)
    private WeekStart weekStart = WeekStart.MONDAY;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
