package com.pleasebookme.server.auth.userrole.repository;

import com.pleasebookme.server.auth.userrole.entity.UserRoleEntity;
import com.pleasebookme.server.auth.userrole.id.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {
}
