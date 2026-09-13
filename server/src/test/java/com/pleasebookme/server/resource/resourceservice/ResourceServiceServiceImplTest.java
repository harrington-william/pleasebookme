package com.pleasebookme.server.resource.resourceservice;

import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.resource.resources.entity.ResourceEntity;
import com.pleasebookme.server.resource.resources.repository.ResourceRepository;
import com.pleasebookme.server.resource.resourceservice.dto.ResourceServiceRequest;
import com.pleasebookme.server.resource.resourceservice.entity.ResourceServiceEntity;
import com.pleasebookme.server.resource.resourceservice.exception.DuplicateResourceServiceException;
import com.pleasebookme.server.resource.resourceservice.exception.ResourceServiceNotFoundException;
import com.pleasebookme.server.resource.resourceservice.id.ResourceServiceId;
import com.pleasebookme.server.resource.resourceservice.repository.ResourceServiceRepository;
import com.pleasebookme.server.resource.resourceservice.service.impl.ResourceServiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceServiceImplTest {
    private static final BigInteger RESOURCE_ID = BigInteger.valueOf(12);
    private static final BigInteger SERVICE_ID = BigInteger.valueOf(5);

    @Mock private ResourceServiceRepository resourceServiceRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private ServiceRepository serviceRepository;

    private ResourceServiceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ResourceServiceServiceImpl(
            resourceServiceRepository,
            resourceRepository,
            serviceRepository
        );
    }

    @Test
    void createResourceService_rejectsDuplicateCompositeKey() {
        when(resourceServiceRepository.existsById(new ResourceServiceId(RESOURCE_ID, SERVICE_ID)))
            .thenReturn(true);

        assertThatThrownBy(() -> service.createResourceService(request()))
            .isInstanceOf(DuplicateResourceServiceException.class);

        verify(resourceRepository, never()).findById(any());
        verify(serviceRepository, never()).findById(any());
        verify(resourceServiceRepository, never()).save(any());
    }

    @Test
    void createResourceService_resolvesBothParentsAndSaves() {
        ResourceEntity resource = ResourceEntity.builder().name("Room A").build();
        resource.setResourceId(RESOURCE_ID);
        ServiceEntity businessService = ServiceEntity.builder().title("Consultation").build();
        businessService.setServiceId(SERVICE_ID);

        when(resourceServiceRepository.existsById(any())).thenReturn(false);
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resource));
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(businessService));
        when(resourceServiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResourceServiceEntity result = service.createResourceService(request());

        assertThat(result.getResource()).isSameAs(resource);
        assertThat(result.getService()).isSameAs(businessService);
    }

    @Test
    void getResourceServiceById_throwsWhenPairDoesNotExist() {
        when(resourceServiceRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getResourceServiceById(RESOURCE_ID, SERVICE_ID))
            .isInstanceOf(ResourceServiceNotFoundException.class);
    }

    @Test
    void getResourceServicesByResourceId_returnsOnlyRepositoryMatches() {
        ResourceServiceEntity first = ResourceServiceEntity.builder().build();
        ResourceServiceEntity second = ResourceServiceEntity.builder().build();
        when(resourceServiceRepository.findByResourceResourceId(RESOURCE_ID))
            .thenReturn(List.of(first, second));

        assertThat(service.getResourceServicesByResourceId(RESOURCE_ID))
            .containsExactly(first, second);
    }

    private static ResourceServiceRequest request() {
        return new ResourceServiceRequest(RESOURCE_ID, SERVICE_ID);
    }
}
