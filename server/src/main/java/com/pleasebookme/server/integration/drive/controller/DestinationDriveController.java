package com.pleasebookme.server.integration.drive.controller;

import com.pleasebookme.server.integration.drive.dto.DestinationDriveRequest;
import com.pleasebookme.server.integration.drive.dto.DestinationDriveResponse;
import com.pleasebookme.server.integration.drive.service.DestinationDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/destination-drives")
@RequiredArgsConstructor
public class DestinationDriveController {
    private final DestinationDriveService destinationDriveService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DestinationDriveResponse createDestinationDrive(@Valid @RequestBody DestinationDriveRequest request) {
        return DestinationDriveResponse.from(destinationDriveService.createDestinationDrive(request));
    }

    @GetMapping("/{destinationDriveId}")
    public DestinationDriveResponse getDestinationDrive(@PathVariable BigInteger destinationDriveId) {
        return DestinationDriveResponse.from(destinationDriveService.getDestinationDriveById(destinationDriveId));
    }

    @GetMapping
    public List<DestinationDriveResponse> getDestinationDrives() {
        return destinationDriveService.getAllDestinationDrives().stream()
            .map(DestinationDriveResponse::from)
            .toList();
    }

    @PutMapping("/{destinationDriveId}")
    public DestinationDriveResponse updateDestinationDrive(
        @PathVariable BigInteger destinationDriveId,
        @Valid @RequestBody DestinationDriveRequest request
    ) {
        return DestinationDriveResponse.from(destinationDriveService.updateDestinationDrive(destinationDriveId, request));
    }

    @DeleteMapping("/{destinationDriveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDestinationDrive(@PathVariable BigInteger destinationDriveId) {
        destinationDriveService.deleteDestinationDrive(destinationDriveId);
    }
}
