package com.pleasebookme.server.customer.customers.service.impl;

import com.pleasebookme.server.customer.customers.dto.CustomerRequest;
import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import com.pleasebookme.server.customer.customers.exception.CustomerNotFoundException;
import com.pleasebookme.server.customer.customers.exception.DuplicateCustomerException;
import com.pleasebookme.server.customer.customers.repository.CustomerRepository;
import com.pleasebookme.server.customer.customers.service.CustomerService;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.tenant.tenants.entity.TenantEntity;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.tenant.tenants.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public CustomerEntity createCustomer(CustomerRequest request) {
        if (customerRepository.existsByTenantTenantIdAndEmail(request.tenantId(), request.email())) {
            throw new DuplicateCustomerException("Email already exists for tenant: " + request.email());
        }

        if (customerRepository.existsByTenantTenantIdAndPhone(request.tenantId(), request.phone())) {
            throw new DuplicateCustomerException("Phone already exists for tenant: " + request.phone());
        }

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + request.tenantId()));

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        CustomerEntity.CustomerEntityBuilder customer = CustomerEntity.builder()
            .tenant(tenant)
            .organization(organization)
            .email(request.email())
            .phone(request.phone())
            .name(request.name())
            .avatarUrl(request.avatarUrl())
            .locale(request.locale())
            .timezone(request.timezone())
            .birthday(request.birthday())
            .gender(request.gender())
            .status(request.status())
            .notes(request.notes());

        if (request.marketingConsent() != null) customer.marketingConsent(request.marketingConsent());

        return customerRepository.save(customer.build());
    }

    @Override
    public CustomerEntity getCustomerById(BigInteger customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(
                "Customer not found: " + customerId
            ));
    }

    @Override
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public CustomerEntity updateCustomer(
        BigInteger customerId,
        CustomerRequest request
    ) {
        CustomerEntity customer = getCustomerById(customerId);

        TenantEntity tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + request.tenantId()));

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new OrganizationNotFoundException("Organization not found: " + request.organizationId()));

        customer.setTenant(tenant);
        customer.setOrganization(organization);
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setName(request.name());
        customer.setAvatarUrl(request.avatarUrl());
        customer.setLocale(request.locale());
        customer.setTimezone(request.timezone());
        customer.setBirthday(request.birthday());
        customer.setGender(request.gender());
        customer.setStatus(request.status());
        customer.setNotes(request.notes());

        if (request.marketingConsent() != null) customer.setMarketingConsent(request.marketingConsent());

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(BigInteger customerId) {
        customerRepository.delete(getCustomerById(customerId));
    }
}
