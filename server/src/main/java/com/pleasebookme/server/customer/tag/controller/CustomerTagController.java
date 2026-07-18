package com.pleasebookme.server.customer.tag.controller;

import com.pleasebookme.server.customer.tag.dto.CustomerTagRequest;
import com.pleasebookme.server.customer.tag.dto.CustomerTagResponse;
import com.pleasebookme.server.customer.tag.service.CustomerTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-tags")
@RequiredArgsConstructor
public class CustomerTagController {
    private final CustomerTagService customerTagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerTagResponse createCustomerTag(@Valid @RequestBody CustomerTagRequest request) {
        return CustomerTagResponse.from(customerTagService.createCustomerTag(request));
    }

    @GetMapping("/{customerTagId}")
    public CustomerTagResponse getCustomerTag(@PathVariable BigInteger customerTagId) {
        return CustomerTagResponse.from(customerTagService.getCustomerTagById(customerTagId));
    }

    @GetMapping
    public List<CustomerTagResponse> getCustomerTags() {
        return customerTagService.getAllCustomerTags().stream()
            .map(CustomerTagResponse::from)
            .toList();
    }

    @PutMapping("/{customerTagId}")
    public CustomerTagResponse updateCustomerTag(
        @PathVariable BigInteger customerTagId,
        @Valid @RequestBody CustomerTagRequest request
    ) {
        return CustomerTagResponse.from(customerTagService.updateCustomerTag(customerTagId, request));
    }

    @DeleteMapping("/{customerTagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerTag(@PathVariable BigInteger customerTagId) {
        customerTagService.deleteCustomerTag(customerTagId);
    }
}
