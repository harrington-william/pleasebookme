package com.pleasebookme.server.customer.tag.service;

import com.pleasebookme.server.customer.tag.dto.CustomerTagRequest;
import com.pleasebookme.server.customer.tag.entity.CustomerTagEntity;

import java.math.BigInteger;
import java.util.List;

public interface CustomerTagService {
    CustomerTagEntity createCustomerTag(CustomerTagRequest request);

    CustomerTagEntity getCustomerTagById(BigInteger customerTagId);

    List<CustomerTagEntity> getAllCustomerTags();

    CustomerTagEntity updateCustomerTag(
        BigInteger customerTagId,
        CustomerTagRequest request
    );

    void deleteCustomerTag(BigInteger customerTagId);
}
