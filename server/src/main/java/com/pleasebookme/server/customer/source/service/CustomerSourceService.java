package com.pleasebookme.server.customer.source.service;

import com.pleasebookme.server.customer.source.dto.CustomerSourceRequest;
import com.pleasebookme.server.customer.source.entity.CustomerSourceEntity;

import java.math.BigInteger;
import java.util.List;

public interface CustomerSourceService {
    CustomerSourceEntity createCustomerSource(CustomerSourceRequest request);

    CustomerSourceEntity getCustomerSourceById(BigInteger customerSourceId);

    List<CustomerSourceEntity> getAllCustomerSources();

    CustomerSourceEntity updateCustomerSource(
        BigInteger customerSourceId,
        CustomerSourceRequest request
    );

    void deleteCustomerSource(BigInteger customerSourceId);
}
