package com.pleasebookme.server.customer.activity.service.impl;

import com.pleasebookme.server.customer.activity.dto.CustomerActivityRequest;
import com.pleasebookme.server.customer.activity.entity.CustomerActivityEntity;
import com.pleasebookme.server.customer.activity.exception.CustomerActivityNotFoundException;
import com.pleasebookme.server.customer.activity.repository.CustomerActivityRepository;
import com.pleasebookme.server.customer.activity.service.CustomerActivityService;
import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import com.pleasebookme.server.customer.customers.exception.CustomerNotFoundException;
import com.pleasebookme.server.customer.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerActivityServiceImpl implements CustomerActivityService {
    private final CustomerActivityRepository customerActivityRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerActivityEntity createCustomerActivity(CustomerActivityRequest request) {
        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        CustomerActivityEntity customerActivity = CustomerActivityEntity.builder()
            .customer(customer)
            .activityType(request.activityType())
            .referenceType(request.referenceType())
            .referenceUid(request.referenceUid())
            .description(request.description())
            .build();

        return customerActivityRepository.save(customerActivity);
    }

    @Override
    public CustomerActivityEntity getCustomerActivityById(BigInteger customerActivityId) {
        return customerActivityRepository.findById(customerActivityId)
            .orElseThrow(() -> new CustomerActivityNotFoundException(
                "Customer activity not found: " + customerActivityId
            ));
    }

    @Override
    public List<CustomerActivityEntity> getAllCustomerActivities() {
        return customerActivityRepository.findAll();
    }

    @Override
    public CustomerActivityEntity updateCustomerActivity(
        BigInteger customerActivityId,
        CustomerActivityRequest request
    ) {
        CustomerActivityEntity customerActivity = getCustomerActivityById(customerActivityId);

        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        customerActivity.setCustomer(customer);
        customerActivity.setActivityType(request.activityType());
        customerActivity.setReferenceType(request.referenceType());
        customerActivity.setReferenceUid(request.referenceUid());
        customerActivity.setDescription(request.description());

        return customerActivityRepository.save(customerActivity);
    }

    @Override
    public void deleteCustomerActivity(BigInteger customerActivityId) {
        customerActivityRepository.delete(getCustomerActivityById(customerActivityId));
    }
}
