package com.pleasebookme.server.core.bookingpolicy.repository;

import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface BookingPolicyRepository extends JpaRepository<BookingPolicyEntity, BigInteger> {
}
