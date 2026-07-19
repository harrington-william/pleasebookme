package com.pleasebookme.server.auth.permission.repository;

import com.pleasebookme.server.auth.permission.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, BigInteger> {
    Optional<PermissionEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
