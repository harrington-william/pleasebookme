package com.pleasebookme.server.core.bookingresource.service;

import com.pleasebookme.server.core.bookingresource.dto.BookingResourceRequest;
import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;

import java.math.BigInteger;
import java.util.List;

public interface BookingResourceService {
    BookingResourceEntity createBookingResource(BookingResourceRequest request);

    BookingResourceEntity getBookingResourceById(
        BigInteger bookingId,
        BigInteger resourceId
    );

    List<BookingResourceEntity> getBookingResourcesByBookingId(BigInteger bookingId);

    List<BookingResourceEntity> getBookingResourcesByResourceId(BigInteger resourceId);

    List<BookingResourceEntity> getBookingResourcesByOrganizationId(BigInteger organizationId);

    void deleteBookingResource(
        BigInteger bookingId,
        BigInteger resourceId
    );
}
