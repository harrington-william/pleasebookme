package com.pleasebookme.server.customer.tag.service.impl;

import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import com.pleasebookme.server.customer.customers.exception.CustomerNotFoundException;
import com.pleasebookme.server.customer.customers.repository.CustomerRepository;
import com.pleasebookme.server.customer.tag.dto.CustomerTagRequest;
import com.pleasebookme.server.customer.tag.entity.CustomerTagEntity;
import com.pleasebookme.server.customer.tag.exception.CustomerTagNotFoundException;
import com.pleasebookme.server.customer.tag.exception.DuplicateCustomerTagException;
import com.pleasebookme.server.customer.tag.repository.CustomerTagRepository;
import com.pleasebookme.server.customer.tag.service.CustomerTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerTagServiceImpl implements CustomerTagService {
    private final CustomerTagRepository customerTagRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerTagEntity createCustomerTag(CustomerTagRequest request) {
        if (customerTagRepository.existsByCustomerCustomerIdAndTag(request.customerId(), request.tag())) {
            throw new DuplicateCustomerTagException("Tag already exists for customer: " + request.tag());
        }

        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        CustomerTagEntity customerTag = CustomerTagEntity.builder()
            .customer(customer)
            .tag(request.tag())
            .build();

        return customerTagRepository.save(customerTag);
    }

    @Override
    public CustomerTagEntity getCustomerTagById(BigInteger customerTagId) {
        return customerTagRepository.findById(customerTagId)
            .orElseThrow(() -> new CustomerTagNotFoundException(
                "Customer tag not found: " + customerTagId
            ));
    }

    @Override
    public List<CustomerTagEntity> getAllCustomerTags() {
        return customerTagRepository.findAll();
    }

    @Override
    public CustomerTagEntity updateCustomerTag(
        BigInteger customerTagId,
        CustomerTagRequest request
    ) {
        CustomerTagEntity customerTag = getCustomerTagById(customerTagId);

        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        customerTag.setCustomer(customer);
        customerTag.setTag(request.tag());

        return customerTagRepository.save(customerTag);
    }

    @Override
    public void deleteCustomerTag(BigInteger customerTagId) {
        customerTagRepository.delete(getCustomerTagById(customerTagId));
    }
}
