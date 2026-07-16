package com.pleasebookme.server.core.booking.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(
    schema = "core",
    name = "bookings"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger bookingId;

    @UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID bookingUid;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Builder.Default
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private UserEntity cancelledBy;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Builder.Default
    @Column(name = "rescheduled", nullable = false)
    private Boolean rescheduled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_by")
    private UserEntity rescheduledBy;

    @Builder.Default
    @Column(name = "no_show_host", nullable = false)
    private Boolean noShowHost = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private UserEntity deletedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_calendar_id")
    private DestinationCalendarEntity destinationCalendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_sheets_id")
    private DestinationSheetsEntity destinationSheets;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
