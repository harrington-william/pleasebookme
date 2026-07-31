package com.pleasebookme.server.integration.syncjob.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;

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
}
