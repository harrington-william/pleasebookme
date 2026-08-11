package com.pleasebookme.server.integration.drive.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.integration.enums.IntegrationType;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(
    schema = "integration",
    name = "destination_drives"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationDriveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger destinationDriveId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "integration_type", nullable = false)
    private IntegrationType integrationType;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_connection_id", nullable = false)
    private OAuthConnectionEntity oauthConnection;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
