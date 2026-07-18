package com.pleasebookme.server.customer.activity.service;

import com.pleasebookme.server.customer.activity.dto.CustomerActivityRequest;
import com.pleasebookme.server.customer.activity.entity.CustomerActivityEntity;

import java.math.BigInteger;
import java.util.List;

public interface CustomerActivityService {
    CustomerActivityEntity createCustomerActivity(CustomerActivityRequest request);

    CustomerActivityEntity getCustomerActivityById(BigInteger customerActivityId);

    List<CustomerActivityEntity> getAllCustomerActivities();

    CustomerActivityEntity updateCustomerActivity(
        BigInteger customerActivityId,
        CustomerActivityRequest request
    );

    void deleteCustomerActivity(BigInteger customerActivityId);
}
