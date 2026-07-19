package com.pleasebookme.server.resource.calendar.repository;

import com.pleasebookme.server.resource.calendar.entity.ResourceCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ResourceCalendarRepository extends JpaRepository<ResourceCalendarEntity, BigInteger> {
}
