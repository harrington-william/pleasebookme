package com.pleasebookme.server.resource.attribute.repository;

import com.pleasebookme.server.resource.attribute.entity.ResourceAttributesEntity;
import com.pleasebookme.server.resource.attribute.id.ResourceAttributesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceAttributeRepository extends JpaRepository<ResourceAttributesEntity, ResourceAttributesId> {
}
