package com.pleasebookme.server.customer.customers.service;

import com.pleasebookme.server.customer.customers.dto.CustomerRequest;
import com.pleasebookme.server.customer.customers.entity.CustomerEntity;

import java.math.BigInteger;
import java.util.List;

public interface CustomerService {
    CustomerEntity createCustomer(CustomerRequest request);

    CustomerEntity getCustomerById(BigInteger customerId);

    List<CustomerEntity> getAllCustomers();

    CustomerEntity updateCustomer(
        BigInteger customerId,
        CustomerRequest request
    );

    void deleteCustomer(BigInteger customerId);
}
