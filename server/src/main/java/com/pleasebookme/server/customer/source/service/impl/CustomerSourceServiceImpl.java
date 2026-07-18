package com.pleasebookme.server.customer.source.service.impl;

import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import com.pleasebookme.server.customer.customers.exception.CustomerNotFoundException;
import com.pleasebookme.server.customer.customers.repository.CustomerRepository;
import com.pleasebookme.server.customer.source.dto.CustomerSourceRequest;
import com.pleasebookme.server.customer.source.entity.CustomerSourceEntity;
import com.pleasebookme.server.customer.source.exception.CustomerSourceNotFoundException;
import com.pleasebookme.server.customer.source.repository.CustomerSourceRepository;
import com.pleasebookme.server.customer.source.service.CustomerSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerSourceServiceImpl implements CustomerSourceService {
    private final CustomerSourceRepository customerSourceRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerSourceEntity createCustomerSource(CustomerSourceRequest request) {
        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        CustomerSourceEntity customerSource = CustomerSourceEntity.builder()
            .customer(customer)
            .source(request.source())
            .build();

        return customerSourceRepository.save(customerSource);
    }

    @Override
    public CustomerSourceEntity getCustomerSourceById(BigInteger customerSourceId) {
        return customerSourceRepository.findById(customerSourceId)
            .orElseThrow(() -> new CustomerSourceNotFoundException(
                "Customer source not found: " + customerSourceId
            ));
    }

    @Override
    public List<CustomerSourceEntity> getAllCustomerSources() {
        return customerSourceRepository.findAll();
    }

    @Override
    public CustomerSourceEntity updateCustomerSource(
        BigInteger customerSourceId,
        CustomerSourceRequest request
    ) {
        CustomerSourceEntity customerSource = getCustomerSourceById(customerSourceId);

        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        customerSource.setCustomer(customer);
        customerSource.setSource(request.source());

        return customerSourceRepository.save(customerSource);
    }

    @Override
    public void deleteCustomerSource(BigInteger customerSourceId) {
        customerSourceRepository.delete(getCustomerSourceById(customerSourceId));
    }
}
