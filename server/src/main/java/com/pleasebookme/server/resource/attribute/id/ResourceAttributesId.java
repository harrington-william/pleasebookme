package com.pleasebookme.server.resource.attribute.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class ResourceAttributesId implements Serializable {
    @Column(name = "resource_id")
    private BigInteger resourceId;

    @Column(name = "key", length = 100)
    private String key;

    public ResourceAttributesId() {}

    public ResourceAttributesId(
        BigInteger resourceId,
        String key
    ) {
        this.resourceId = resourceId;
        this.key = key;
    }

    public BigInteger getResourceId() { return resourceId; }
    public void setResourceId(BigInteger resourceId) { this.resourceId = resourceId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAttributesId that = (ResourceAttributesId) o;
        return Objects.equals(resourceId, that.resourceId) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, key);
    }
}
