package com.pleasebookme.server.core.booking.dto;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public record BookingPageResponse(
    List<BookingResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static BookingPageResponse from(Page<BookingEntity> bookings) {
        return new BookingPageResponse(
            bookings.getContent().stream().map(BookingResponse::from).toList(),
            bookings.getNumber(),
            bookings.getSize(),
            bookings.getTotalElements(),
            bookings.getTotalPages()
        );
    }
}
