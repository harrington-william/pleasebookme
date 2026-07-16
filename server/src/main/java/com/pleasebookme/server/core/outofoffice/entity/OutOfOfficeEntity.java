package com.pleasebookme.server.core.outofoffice.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
    schema = "core",
    name = "out_of_office"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutOfOfficeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger outOfOfficeId;

    @UuidGenerator
    @Column(name = "uid", nullable = false, unique = true, updatable = false)
    private UUID outOfOfficeUid;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "notes")
    private String notes;

    @Builder.Default
    @Column(name = "show_note_publicly", nullable = false)
    private Boolean showNotePublicly = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private UserEntity toUser;

    @Column(name = "reason", nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
