package com.pleasebookme.server.core.outofoffice.controller;

import com.pleasebookme.server.core.outofoffice.dto.OutOfOfficeRequest;
import com.pleasebookme.server.core.outofoffice.dto.OutOfOfficeResponse;
import com.pleasebookme.server.core.outofoffice.service.OutOfOfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ooo")
@RequiredArgsConstructor
public class OutOfOfficeController {
    private final OutOfOfficeService outOfOfficeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OutOfOfficeResponse createOutOfOffice(@Valid @RequestBody OutOfOfficeRequest request) {
        return OutOfOfficeResponse.from(outOfOfficeService.createOutOfOffice(request));
    }

    @GetMapping("/{outOfOfficeId}")
    public OutOfOfficeResponse getOutOfOffice(@PathVariable BigInteger outOfOfficeId) {
        return OutOfOfficeResponse.from(outOfOfficeService.getOutOfOfficeById(outOfOfficeId));
    }

    @GetMapping
    public List<OutOfOfficeResponse> getOutOfOffices() {
        return outOfOfficeService.getAllOutOfOffices().stream()
            .map(OutOfOfficeResponse::from)
            .toList();
    }

    @PutMapping("/{outOfOfficeId}")
    public OutOfOfficeResponse updateOutOfOffice(
        @PathVariable BigInteger outOfOfficeId,
        @Valid @RequestBody OutOfOfficeRequest request
    ) {
        return OutOfOfficeResponse.from(outOfOfficeService.updateOutOfOffice(outOfOfficeId, request));
    }

    @DeleteMapping("/{outOfOfficeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOutOfOffice(@PathVariable BigInteger outOfOfficeId) {
        outOfOfficeService.deleteOutOfOffice(outOfOfficeId);
    }
}
