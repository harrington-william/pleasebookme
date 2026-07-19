package com.pleasebookme.server.integration.sheets.service;

import com.pleasebookme.server.integration.sheets.dto.DestinationSheetsRequest;
import com.pleasebookme.server.integration.sheets.entity.DestinationSheetsEntity;

import java.math.BigInteger;
import java.util.List;

public interface DestinationSheetsService {
    DestinationSheetsEntity createDestinationSheets(DestinationSheetsRequest request);

    DestinationSheetsEntity getDestinationSheetsById(BigInteger destinationSheetsId);

    List<DestinationSheetsEntity> getAllDestinationSheets();

    DestinationSheetsEntity updateDestinationSheets(
        BigInteger destinationSheetsId,
        DestinationSheetsRequest request
    );

    void deleteDestinationSheets(BigInteger destinationSheetsId);
}
