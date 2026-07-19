package com.pleasebookme.server.notification.queue.entity;

import com.pleasebookme.server.notification.enums.NotificationStatus;
import com.pleasebookme.server.notification.notifications.entity.NotificationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(
    schema = "notification",
    name = "notification_queue"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger notificationQueueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private NotificationEntity notification;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.QUEUED;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
