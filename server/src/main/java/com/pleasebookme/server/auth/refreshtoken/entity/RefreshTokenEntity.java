package com.pleasebookme.server.auth.refreshtoken.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(
    schema = "auth",
    name = "refresh_tokens"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Missing widget
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger refreshTokenId;

    @Column(name = "secret", nullable = false)
    private String secret;

    @Column(name = "owner")
    private String owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "widget_id")
    private BigInteger widgetId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "oauth_client_id")
    private String oauthClientId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
