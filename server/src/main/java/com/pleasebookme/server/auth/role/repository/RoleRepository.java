package com.pleasebookme.server.auth.role.repository;

import com.pleasebookme.server.auth.role.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, BigInteger> {
    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);
}
