package com.pleasebookme.server.customer.tag.dto;

import com.pleasebookme.server.customer.tag.entity.CustomerTagEntity;

import java.math.BigInteger;
import java.time.Instant;

public record CustomerTagResponse(
    BigInteger customerTagId,
    BigInteger customerId,
    String tag,
    Instant createdAt,
    Instant updatedAt
) {
    public static CustomerTagResponse from(CustomerTagEntity customerTag) {
        return new CustomerTagResponse(
            customerTag.getCustomerTagId(),
            customerTag.getCustomer().getCustomerId(),
            customerTag.getTag(),
            customerTag.getCreatedAt(),
            customerTag.getUpdatedAt()
        );
    }
}
