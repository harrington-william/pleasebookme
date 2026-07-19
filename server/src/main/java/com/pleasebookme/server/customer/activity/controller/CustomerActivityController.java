package com.pleasebookme.server.customer.activity.controller;

import com.pleasebookme.server.customer.activity.dto.CustomerActivityRequest;
import com.pleasebookme.server.customer.activity.dto.CustomerActivityResponse;
import com.pleasebookme.server.customer.activity.service.CustomerActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-activities")
@RequiredArgsConstructor
public class CustomerActivityController {
    private final CustomerActivityService customerActivityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerActivityResponse createCustomerActivity(@Valid @RequestBody CustomerActivityRequest request) {
        return CustomerActivityResponse.from(customerActivityService.createCustomerActivity(request));
    }

    @GetMapping("/{customerActivityId}")
    public CustomerActivityResponse getCustomerActivity(@PathVariable BigInteger customerActivityId) {
        return CustomerActivityResponse.from(customerActivityService.getCustomerActivityById(customerActivityId));
    }

    @GetMapping
    public List<CustomerActivityResponse> getCustomerActivities() {
        return customerActivityService.getAllCustomerActivities().stream()
            .map(CustomerActivityResponse::from)
            .toList();
    }

    @PutMapping("/{customerActivityId}")
    public CustomerActivityResponse updateCustomerActivity(
        @PathVariable BigInteger customerActivityId,
        @Valid @RequestBody CustomerActivityRequest request
    ) {
        return CustomerActivityResponse.from(customerActivityService.updateCustomerActivity(customerActivityId, request));
    }

    @DeleteMapping("/{customerActivityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerActivity(@PathVariable BigInteger customerActivityId) {
        customerActivityService.deleteCustomerActivity(customerActivityId);
    }
}
