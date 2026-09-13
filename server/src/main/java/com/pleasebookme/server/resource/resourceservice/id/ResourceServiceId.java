package com.pleasebookme.server.resource.resourceservice.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class ResourceServiceId implements Serializable {
    @Column(name = "resource_id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger resourceId;

    @Column(name = "service_id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger serviceId;

    public ResourceServiceId() {}

    public ResourceServiceId(
        BigInteger resourceId,
        BigInteger serviceId
    ) {
        this.resourceId = resourceId;
        this.serviceId = serviceId;
    }

    public BigInteger getResourceId() { return resourceId; }
    public void setResourceId(BigInteger resourceId) { this.resourceId = resourceId; }

    public BigInteger getServiceId() { return serviceId; }
    public void setServiceId(BigInteger serviceId) { this.serviceId = serviceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceServiceId that = (ResourceServiceId) o;
        return Objects.equals(resourceId, that.resourceId)
            && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, serviceId);
    }
}
