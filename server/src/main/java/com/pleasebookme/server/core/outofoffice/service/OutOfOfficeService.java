package com.pleasebookme.server.core.outofoffice.service;

import com.pleasebookme.server.core.outofoffice.dto.OutOfOfficeRequest;
import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;

import java.math.BigInteger;
import java.util.List;

public interface OutOfOfficeService {
    OutOfOfficeEntity createOutOfOffice(OutOfOfficeRequest request);

    OutOfOfficeEntity getOutOfOfficeById(BigInteger outOfOfficeId);

    List<OutOfOfficeEntity> getAllOutOfOffices();

    OutOfOfficeEntity updateOutOfOffice(
        BigInteger outOfOfficeId,
        OutOfOfficeRequest request
    );

    void deleteOutOfOffice(BigInteger outOfOfficeId);
}
