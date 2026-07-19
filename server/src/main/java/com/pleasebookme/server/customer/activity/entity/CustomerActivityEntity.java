package com.pleasebookme.server.customer.activity.entity;

import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(
    schema = "customer",
    name = "customer_activities"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger customerActivityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType;

    @Column(name = "reference_type", length = 50, nullable = false)
    private String referenceType;

    @Column(name = "reference_uid", length = 255, nullable = false)
    private String referenceUid;

    @Column(name = "description")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
}
