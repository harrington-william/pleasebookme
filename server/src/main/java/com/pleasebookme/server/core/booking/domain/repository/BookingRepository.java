package com.pleasebookme.server.core.booking.domain.repository;

import com.pleasebookme.server.core.booking.domain.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface BookingRepository
    extends JpaRepository<BookingEntity, BigInteger>, JpaSpecificationExecutor<BookingEntity> {
}
