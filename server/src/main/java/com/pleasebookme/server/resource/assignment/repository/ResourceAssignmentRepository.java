package com.pleasebookme.server.resource.assignment.repository;

import com.pleasebookme.server.resource.assignment.entity.ResourceAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ResourceAssignmentRepository extends JpaRepository<ResourceAssignmentEntity, BigInteger> {
}
