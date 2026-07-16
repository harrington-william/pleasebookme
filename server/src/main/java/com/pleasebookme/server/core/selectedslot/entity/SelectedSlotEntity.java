package com.pleasebookme.server.core.selectedslot.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
    schema = "core",
    name = "selected_slots"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger selectedSlotId;

    @UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID selectedSlotUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "slot_start", nullable = false)
    private Instant slotStart;

    @Column(name = "slot_end", nullable = false)
    private Instant slotEnd;

    @Column(name = "release_at", nullable = false)
    private Instant releaseAt;

    @Builder.Default
    @Column(name = "is_seat", nullable = false)
    private Boolean isSeat = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
