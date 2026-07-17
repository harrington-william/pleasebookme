package com.pleasebookme.server.integration.calendar.repository;

import com.pleasebookme.server.integration.calendar.entity.DestinationCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface DestinationCalendarRepository extends JpaRepository<DestinationCalendarEntity, BigInteger> {
}
