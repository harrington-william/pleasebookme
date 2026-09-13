package com.pleasebookme.server.core.booking.repository;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface BookingRepository
    extends JpaRepository<BookingEntity, BigInteger>, JpaSpecificationExecutor<BookingEntity> {
}
