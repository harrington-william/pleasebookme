package com.pleasebookme.server.customer.customers.dto;

import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import com.pleasebookme.server.global.enums.Locale;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
    BigInteger customerId,
    UUID customerUid,
    BigInteger tenantId,
    BigInteger organizationId,
    String email,
    String phone,
    String name,
    String avatarUrl,
    Locale locale,
    String timezone,
    LocalDate birthday,
    String gender,
    String status,
    Boolean marketingConsent,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
    public static CustomerResponse from(CustomerEntity customer) {
        return new CustomerResponse(
            customer.getCustomerId(),
            customer.getCustomerUid(),
            customer.getTenant().getTenantId(),
            customer.getOrganization().getOrganizationId(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getName(),
            customer.getAvatarUrl(),
            customer.getLocale(),
            customer.getTimezone(),
            customer.getBirthday(),
            customer.getGender(),
            customer.getStatus(),
            customer.getMarketingConsent(),
            customer.getNotes(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
