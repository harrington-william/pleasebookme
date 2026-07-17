package com.pleasebookme.server.integration.sheets.repository;

import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface DestinationSheetsRepository extends JpaRepository<DestinationSheetsEntity, BigInteger> {
}
