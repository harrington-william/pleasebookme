package com.pleasebookme.server.core.outofoffice.repository;

import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface OutOfOfficeRepository extends JpaRepository<OutOfOfficeEntity, BigInteger> {
}
