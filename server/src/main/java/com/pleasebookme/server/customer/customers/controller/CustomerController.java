package com.pleasebookme.server.customer.customers.controller;

import com.pleasebookme.server.customer.customers.dto.CustomerRequest;
import com.pleasebookme.server.customer.customers.dto.CustomerResponse;
import com.pleasebookme.server.customer.customers.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return CustomerResponse.from(customerService.createCustomer(request));
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable BigInteger customerId) {
        return CustomerResponse.from(customerService.getCustomerById(customerId));
    }

    @GetMapping
    public List<CustomerResponse> getCustomers() {
        return customerService.getAllCustomers().stream()
            .map(CustomerResponse::from)
            .toList();
    }

    @PutMapping("/{customerId}")
    public CustomerResponse updateCustomer(
        @PathVariable BigInteger customerId,
        @Valid @RequestBody CustomerRequest request
    ) {
        return CustomerResponse.from(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable BigInteger customerId) {
        customerService.deleteCustomer(customerId);
    }
}
