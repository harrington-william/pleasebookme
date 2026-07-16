package com.pleasebookme.server.audit.actor.entity;

import com.pleasebookme.server.audit.enums.AuditActorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Instant;

@Entity
@Table(
    schema = "audit",
    name = "audit_actors"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditActorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger auditActorId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "actor_type", nullable = false)
    private AuditActorType actorType;

    @Column(name = "user_uid", unique = true)
    private String userUid;

    @Column(name = "membership_id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger membershipId;

    @Column(name = "widget_uid")
    private String widgetUid;

    @Column(name = "api_key_uid")
    private String apiKeyUid;

    @Column(name = "attendee_id", unique = true)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger attendeeId;

    @Column(name = "system_name", length = 100)
    private String systemName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "email", unique = true)
    private String email;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address", nullable = false)
    private InetAddress ipAddress;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
