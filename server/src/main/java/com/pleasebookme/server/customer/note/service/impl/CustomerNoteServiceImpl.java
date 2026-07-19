package com.pleasebookme.server.customer.note.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.customer.customers.entity.CustomerEntity;
import com.pleasebookme.server.customer.customers.exception.CustomerNotFoundException;
import com.pleasebookme.server.customer.customers.repository.CustomerRepository;
import com.pleasebookme.server.customer.note.dto.CustomerNoteRequest;
import com.pleasebookme.server.customer.note.entity.CustomerNoteEntity;
import com.pleasebookme.server.customer.note.exception.CustomerNoteNotFoundException;
import com.pleasebookme.server.customer.note.repository.CustomerNoteRepository;
import com.pleasebookme.server.customer.note.service.CustomerNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerNoteServiceImpl implements CustomerNoteService {
    private final CustomerNoteRepository customerNoteRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public CustomerNoteEntity createCustomerNote(CustomerNoteRequest request) {
        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        CustomerNoteEntity customerNote = CustomerNoteEntity.builder()
            .customer(customer)
            .authorUser(resolveUser(request.authorUserId()))
            .content(request.content())
            .build();

        return customerNoteRepository.save(customerNote);
    }

    @Override
    public CustomerNoteEntity getCustomerNoteById(BigInteger customerNoteId) {
        return customerNoteRepository.findById(customerNoteId)
            .orElseThrow(() -> new CustomerNoteNotFoundException(
                "Customer note not found: " + customerNoteId
            ));
    }

    @Override
    public List<CustomerNoteEntity> getAllCustomerNotes() {
        return customerNoteRepository.findAll();
    }

    @Override
    public CustomerNoteEntity updateCustomerNote(
        BigInteger customerNoteId,
        CustomerNoteRequest request
    ) {
        CustomerNoteEntity customerNote = getCustomerNoteById(customerNoteId);

        CustomerEntity customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.customerId()));

        customerNote.setCustomer(customer);
        customerNote.setAuthorUser(resolveUser(request.authorUserId()));
        customerNote.setContent(request.content());

        return customerNoteRepository.save(customerNote);
    }

    @Override
    public void deleteCustomerNote(BigInteger customerNoteId) {
        customerNoteRepository.delete(getCustomerNoteById(customerNoteId));
    }

    private UserEntity resolveUser(BigInteger userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
