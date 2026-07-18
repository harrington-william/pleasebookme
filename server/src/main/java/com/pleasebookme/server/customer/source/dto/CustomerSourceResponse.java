package com.pleasebookme.server.customer.source.dto;

import com.pleasebookme.server.customer.source.entity.CustomerSourceEntity;

import java.math.BigInteger;
import java.time.Instant;

public record CustomerSourceResponse(
    BigInteger customerSourceId,
    BigInteger customerId,
    String source,
    Instant createdAt
) {
    public static CustomerSourceResponse from(CustomerSourceEntity customerSource) {
        return new CustomerSourceResponse(
            customerSource.getCustomerSourceId(),
            customerSource.getCustomer().getCustomerId(),
            customerSource.getSource(),
            customerSource.getCreatedAt()
        );
    }
}
