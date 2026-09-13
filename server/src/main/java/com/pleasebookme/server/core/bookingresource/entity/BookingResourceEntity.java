package com.pleasebookme.server.core.bookingresource.entity;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.bookingresource.id.BookingResourceId;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
    schema = "core",
    name = "booking_resources"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResourceEntity {
    @Builder.Default
    @EmbeddedId
    private BookingResourceId bookingResourceId = new BookingResourceId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id", nullable = false)
    private ResourceEntity resource;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;
}
