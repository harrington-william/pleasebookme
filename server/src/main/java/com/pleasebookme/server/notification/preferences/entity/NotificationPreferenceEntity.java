package com.pleasebookme.server.notification.preferences.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    schema = "notification",
    name = "notification_preferences"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger notificationPreferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Builder.Default
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = false;

    @Builder.Default
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @Builder.Default
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = false;

    @Builder.Default
    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = false;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
