package com.pleasebookme.server.integration.sheets.controller;

import com.pleasebookme.server.integration.sheets.dto.DestinationSheetsRequest;
import com.pleasebookme.server.integration.sheets.dto.DestinationSheetsResponse;
import com.pleasebookme.server.integration.sheets.service.DestinationSheetsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/destination-sheets")
@RequiredArgsConstructor
public class DestinationSheetsController {
    private final DestinationSheetsService destinationSheetsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DestinationSheetsResponse createDestinationSheets(@Valid @RequestBody DestinationSheetsRequest request) {
        return DestinationSheetsResponse.from(destinationSheetsService.createDestinationSheets(request));
    }

    @GetMapping("/{destinationSheetsId}")
    public DestinationSheetsResponse getDestinationSheets(@PathVariable BigInteger destinationSheetsId) {
        return DestinationSheetsResponse.from(destinationSheetsService.getDestinationSheetsById(destinationSheetsId));
    }

    @GetMapping
    public List<DestinationSheetsResponse> getDestinationSheets() {
        return destinationSheetsService.getAllDestinationSheets().stream()
            .map(DestinationSheetsResponse::from)
            .toList();
    }

    @PutMapping("/{destinationSheetsId}")
    public DestinationSheetsResponse updateDestinationSheets(
        @PathVariable BigInteger destinationSheetsId,
        @Valid @RequestBody DestinationSheetsRequest request
    ) {
        return DestinationSheetsResponse.from(destinationSheetsService.updateDestinationSheets(destinationSheetsId, request));
    }

    @DeleteMapping("/{destinationSheetsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDestinationSheets(@PathVariable BigInteger destinationSheetsId) {
        destinationSheetsService.deleteDestinationSheets(destinationSheetsId);
    }
}
