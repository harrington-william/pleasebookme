package com.pleasebookme.server.customer.source.controller;

import com.pleasebookme.server.customer.source.dto.CustomerSourceRequest;
import com.pleasebookme.server.customer.source.dto.CustomerSourceResponse;
import com.pleasebookme.server.customer.source.service.CustomerSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-sources")
@RequiredArgsConstructor
public class CustomerSourceController {
    private final CustomerSourceService customerSourceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerSourceResponse createCustomerSource(@Valid @RequestBody CustomerSourceRequest request) {
        return CustomerSourceResponse.from(customerSourceService.createCustomerSource(request));
    }

    @GetMapping("/{customerSourceId}")
    public CustomerSourceResponse getCustomerSource(@PathVariable BigInteger customerSourceId) {
        return CustomerSourceResponse.from(customerSourceService.getCustomerSourceById(customerSourceId));
    }

    @GetMapping
    public List<CustomerSourceResponse> getCustomerSources() {
        return customerSourceService.getAllCustomerSources().stream()
            .map(CustomerSourceResponse::from)
            .toList();
    }

    @PutMapping("/{customerSourceId}")
    public CustomerSourceResponse updateCustomerSource(
        @PathVariable BigInteger customerSourceId,
        @Valid @RequestBody CustomerSourceRequest request
    ) {
        return CustomerSourceResponse.from(customerSourceService.updateCustomerSource(customerSourceId, request));
    }

    @DeleteMapping("/{customerSourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerSource(@PathVariable BigInteger customerSourceId) {
        customerSourceService.deleteCustomerSource(customerSourceId);
    }
}
