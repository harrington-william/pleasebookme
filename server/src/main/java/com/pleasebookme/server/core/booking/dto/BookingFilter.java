package com.pleasebookme.server.core.booking.dto;

import java.math.BigInteger;

public record BookingFilter(
    BigInteger serviceId,
    BigInteger resourceId,
    String q
) {
    public static BookingFilter none() {
        return new BookingFilter(null, null, null);
    }
}
