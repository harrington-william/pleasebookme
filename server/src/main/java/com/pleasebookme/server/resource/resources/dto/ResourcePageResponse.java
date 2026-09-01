package com.pleasebookme.server.resource.resources.dto;

import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public record ResourcePageResponse(
    List<ResourceResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static ResourcePageResponse from(Page<ResourceEntity> resources) {
        return new ResourcePageResponse(
            resources.getContent().stream().map(ResourceResponse::from).toList(),
            resources.getNumber(),
            resources.getSize(),
            resources.getTotalElements(),
            resources.getTotalPages()
        );
    }
}
