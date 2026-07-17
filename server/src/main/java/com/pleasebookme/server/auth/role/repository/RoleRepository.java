package com.pleasebookme.server.auth.role.repository;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, BigInteger> {
    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);
}
