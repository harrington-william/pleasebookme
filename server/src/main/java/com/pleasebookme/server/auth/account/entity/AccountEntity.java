package com.pleasebookme.server.auth.account.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    schema = "auth",
    name = "accounts"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Builder.Default
    @Column(name = "type", nullable = false, length = 50)
    private String type = "oauth";

    @Column(name = "provider", nullable = false, length = 100)
    private String provider;

    @Column(name = "provider_account_id", nullable = false, length = 255)
    private String providerAccountId;

    @Column(name = "provider_email", length = 255)
    private String providerEmail;

}
