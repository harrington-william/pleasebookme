package com.pleasebookme.server.customer.note.dto;

import com.pleasebookme.server.customer.note.entity.CustomerNoteEntity;

import java.math.BigInteger;
import java.time.Instant;

public record CustomerNoteResponse(
    BigInteger customerNoteId,
    BigInteger customerId,
    BigInteger authorUserId,
    String content,
    Instant createdAt
) {
    public static CustomerNoteResponse from(CustomerNoteEntity customerNote) {
        return new CustomerNoteResponse(
            customerNote.getCustomerNoteId(),
            customerNote.getCustomer().getCustomerId(),
            customerNote.getAuthorUser() != null ? customerNote.getAuthorUser().getUserId() : null,
            customerNote.getContent(),
            customerNote.getCreatedAt()
        );
    }
}
