package com.pleasebookme.server.core.bookingresource.repository;

import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;
import com.pleasebookme.server.core.bookingresource.id.BookingResourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingResourceRepository extends JpaRepository<BookingResourceEntity, BookingResourceId> {
    List<BookingResourceEntity> findByBookingBookingId(BigInteger bookingId);

    List<BookingResourceEntity> findByResourceResourceId(BigInteger resourceId);

    List<BookingResourceEntity> findByBookingServiceOrganizationOrganizationId(BigInteger organizationId);

    Optional<BookingResourceEntity> findByBookingBookingIdAndIsPrimaryTrue(BigInteger bookingId);
}
