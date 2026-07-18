package com.pleasebookme.server.customer.note.controller;

import com.pleasebookme.server.customer.note.dto.CustomerNoteRequest;
import com.pleasebookme.server.customer.note.dto.CustomerNoteResponse;
import com.pleasebookme.server.customer.note.service.CustomerNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-notes")
@RequiredArgsConstructor
public class CustomerNoteController {
    private final CustomerNoteService customerNoteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerNoteResponse createCustomerNote(@Valid @RequestBody CustomerNoteRequest request) {
        return CustomerNoteResponse.from(customerNoteService.createCustomerNote(request));
    }

    @GetMapping("/{customerNoteId}")
    public CustomerNoteResponse getCustomerNote(@PathVariable BigInteger customerNoteId) {
        return CustomerNoteResponse.from(customerNoteService.getCustomerNoteById(customerNoteId));
    }

    @GetMapping
    public List<CustomerNoteResponse> getCustomerNotes() {
        return customerNoteService.getAllCustomerNotes().stream()
            .map(CustomerNoteResponse::from)
            .toList();
    }

    @PutMapping("/{customerNoteId}")
    public CustomerNoteResponse updateCustomerNote(
        @PathVariable BigInteger customerNoteId,
        @Valid @RequestBody CustomerNoteRequest request
    ) {
        return CustomerNoteResponse.from(customerNoteService.updateCustomerNote(customerNoteId, request));
    }

    @DeleteMapping("/{customerNoteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerNote(@PathVariable BigInteger customerNoteId) {
        customerNoteService.deleteCustomerNote(customerNoteId);
    }
}
