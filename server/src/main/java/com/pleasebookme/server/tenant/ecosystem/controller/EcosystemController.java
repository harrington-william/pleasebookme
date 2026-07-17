package com.pleasebookme.server.tenant.ecosystem.controller;

import com.pleasebookme.server.tenant.ecosystem.dto.EcosystemRequest;
import com.pleasebookme.server.tenant.ecosystem.dto.EcosystemResponse;
import com.pleasebookme.server.tenant.ecosystem.service.EcosystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ecosystems")
@RequiredArgsConstructor
public class EcosystemController {
    private final EcosystemService ecosystemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcosystemResponse createEcosystem(@Valid @RequestBody EcosystemRequest request) {
        return EcosystemResponse.from(ecosystemService.createEcosystem(request));
    }

    @GetMapping("/{ecosystemId}")
    public EcosystemResponse getEcosystem(@PathVariable BigInteger ecosystemId) {
        return EcosystemResponse.from(ecosystemService.getEcosystemById(ecosystemId));
    }

    @GetMapping
    public List<EcosystemResponse> getEcosystems() {
        return ecosystemService.getAllEcosystems().stream()
            .map(EcosystemResponse::from)
            .toList();
    }

    @PutMapping("/{ecosystemId}")
    public EcosystemResponse updateEcosystem(
        @PathVariable BigInteger ecosystemId,
        @Valid @RequestBody EcosystemRequest request
    ) {
        return EcosystemResponse.from(ecosystemService.updateEcosystem(ecosystemId, request));
    }

    @DeleteMapping("/{ecosystemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEcosystem(@PathVariable BigInteger ecosystemId) {
        ecosystemService.deleteEcosystem(ecosystemId);
    }
}
