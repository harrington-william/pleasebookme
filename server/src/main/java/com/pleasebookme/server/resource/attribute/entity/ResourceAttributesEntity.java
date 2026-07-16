package com.pleasebookme.server.resource.attribute.entity;

import com.pleasebookme.server.resource.attribute.id.ResourceAttributesId;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
    schema = "resource",
    name = "resource_attributes"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAttributesEntity {
    @Builder.Default
    @EmbeddedId
    private ResourceAttributesId resourceAttributesId = new ResourceAttributesId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id")
    private ResourceEntity resource;

    @Column(name = "value", nullable = false)
    private String value;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
