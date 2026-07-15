package com.pleasebookme.server.tenant.plan.entity;

import com.pleasebookme.server.global.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(
    schema = "tenant",
    name = "plans"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger tenantPlanId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.USD;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "max_services", nullable = false)
    private Integer maxServices;

    @Column(name = "max_widgets", nullable = false)
    private Integer maxWidgets;

    @Column(name = "max_resources", nullable = false)
    private Integer maxResources;

    @Column(name = "max_api_keys", nullable = false)
    private Integer maxApiKeys;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features")
    private JsonNode features;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
