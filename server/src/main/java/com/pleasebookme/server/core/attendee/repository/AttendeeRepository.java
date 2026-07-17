package com.pleasebookme.server.core.attendee.repository;

import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface AttendeeRepository extends JpaRepository<AttendeeEntity, BigInteger> {
}
