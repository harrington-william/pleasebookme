package com.pleasebookme.server.customer.note.service;

import com.pleasebookme.server.customer.note.dto.CustomerNoteRequest;
import com.pleasebookme.server.customer.note.entity.CustomerNoteEntity;

import java.math.BigInteger;
import java.util.List;

public interface CustomerNoteService {
    CustomerNoteEntity createCustomerNote(CustomerNoteRequest request);

    CustomerNoteEntity getCustomerNoteById(BigInteger customerNoteId);

    List<CustomerNoteEntity> getAllCustomerNotes();

    CustomerNoteEntity updateCustomerNote(
        BigInteger customerNoteId,
        CustomerNoteRequest request
    );

    void deleteCustomerNote(BigInteger customerNoteId);
}
