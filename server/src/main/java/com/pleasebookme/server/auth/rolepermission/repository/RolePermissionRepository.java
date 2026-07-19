package com.pleasebookme.server.auth.rolepermission.repository;

import com.pleasebookme.server.auth.rolepermission.entity.RolePermissionEntity;
import com.pleasebookme.server.auth.rolepermission.id.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {
}
