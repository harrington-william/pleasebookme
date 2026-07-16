package com.pleasebookme.server.core.bookingpolicy.entity;

import com.pleasebookme.server.core.enums.BookingMode;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(
    schema = "core",
    name = "booking_policies"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger bookingPolicyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "booking_mode", nullable = false)
    private BookingMode bookingMode = BookingMode.FIXED;

    @Column(name = "duration_type", nullable = false, length = 50)
    private String durationType;

    @Builder.Default
    @Column(name = "default_duration", nullable = false)
    private Integer defaultDuration = 1;

    @Column(name = "minimum_duration")
    private Integer minimumDuration;

    @Column(name = "maximum_duration")
    private Integer maximumDuration;

    @Column(name = "minimum_notice", nullable = false)
    private Integer minimumNotice;

    @Column(name = "maximum_advance_booking", nullable = false)
    private Integer maximumAdvanceBooking;

    @Builder.Default
    @Column(name = "slot_interval", nullable = false)
    private Integer slotInterval = 30;

    @Builder.Default
    @Column(name = "before_buffer", nullable = false)
    private Integer beforeBuffer = 0;

    @Builder.Default
    @Column(name = "after_buffer", nullable = false)
    private Integer afterBuffer = 0;

    @Builder.Default
    @Column(name = "allow_overlap", nullable = false)
    private Boolean allowOverlap = false;

    @Builder.Default
    @Column(name = "allow_multiple_attendee", nullable = false)
    private Boolean allowMultipleAttendee = false;

    @Builder.Default
    @Column(name = "requires_payment", nullable = false)
    private Boolean requiresPayment = false;

    @Builder.Default
    @Column(name = "auto_confirm", nullable = false)
    private Boolean autoConfirm = true;

    @Column(name = "booking_window_type", nullable = false, length = 50)
    private String bookingWindowType;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
