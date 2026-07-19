package com.pleasebookme.server.widget.widgetorigin.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    schema = "widget",
    name = "widget_origins"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetOriginEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger widgetOriginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_id", nullable = false)
    private WidgetEntity widget;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    private UserEntity createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
