package com.pleasebookme.server.integration.syncjob.entity;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.integration.enums.SyncJobStatus;
import com.pleasebookme.server.integration.enums.SyncJobType;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    schema = "integration",
    name = "sync_jobs"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger syncJobId;

    @UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID syncJobUid;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "job_type", nullable = false)
    private SyncJobType jobType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_connection_id", nullable = false)
    private OAuthConnectionEntity oauthConnection;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private SyncJobStatus status = SyncJobStatus.PENDING;

    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Builder.Default
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 5;

    @Builder.Default
    @Column(name = "available_at", nullable = false)
    private Instant availableAt = Instant.now();

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "last_error")
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
