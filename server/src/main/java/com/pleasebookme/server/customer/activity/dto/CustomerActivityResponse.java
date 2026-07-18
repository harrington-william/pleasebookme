package com.pleasebookme.server.customer.activity.dto;

import com.pleasebookme.server.customer.activity.entity.CustomerActivityEntity;

import java.math.BigInteger;
import java.time.Instant;

public record CustomerActivityResponse(
    BigInteger customerActivityId,
    BigInteger customerId,
    String activityType,
    String referenceType,
    String referenceUid,
    String description,
    Instant occurredAt
) {
    public static CustomerActivityResponse from(CustomerActivityEntity customerActivity) {
        return new CustomerActivityResponse(
            customerActivity.getCustomerActivityId(),
            customerActivity.getCustomer().getCustomerId(),
            customerActivity.getActivityType(),
            customerActivity.getReferenceType(),
            customerActivity.getReferenceUid(),
            customerActivity.getDescription(),
            customerActivity.getOccurredAt()
        );
    }
}
