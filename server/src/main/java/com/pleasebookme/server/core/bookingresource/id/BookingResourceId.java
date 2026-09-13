package com.pleasebookme.server.core.bookingresource.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Embeddable
public class BookingResourceId implements Serializable {
    @Column(name = "booking_id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger bookingId;

    @Column(name = "resource_id")
    @JdbcTypeCode(SqlTypes.BIGINT)
    private BigInteger resourceId;

    public BookingResourceId() {}

    public BookingResourceId(
        BigInteger bookingId,
        BigInteger resourceId
    ) {
        this.bookingId = bookingId;
        this.resourceId = resourceId;
    }

    public BigInteger getBookingId() { return bookingId; }
    public void setBookingId(BigInteger bookingId) { this.bookingId = bookingId; }

    public BigInteger getResourceId() { return resourceId; }
    public void setResourceId(BigInteger resourceId) { this.resourceId = resourceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingResourceId that = (BookingResourceId) o;
        return Objects.equals(bookingId, that.bookingId)
            && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, resourceId);
    }
}
