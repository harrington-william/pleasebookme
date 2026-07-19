package com.pleasebookme.server.core.schedule.repository;

import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, BigInteger> {
}
