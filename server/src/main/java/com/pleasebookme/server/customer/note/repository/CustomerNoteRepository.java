package com.pleasebookme.server.customer.note.repository;

import com.pleasebookme.server.customer.note.entity.CustomerNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNoteEntity, BigInteger> {
}
