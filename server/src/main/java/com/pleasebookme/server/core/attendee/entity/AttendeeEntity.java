package com.pleasebookme.server.core.attendee.entity;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.global.enums.Locale;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    schema = "core",
    name = "attendees"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger attendeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "locale")
    private Locale locale;

    @Column(name = "timezone", length = 100)
    private String timezone;

    @Builder.Default
    @Column(name = "no_show", nullable = false)
    private Boolean noShow = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
