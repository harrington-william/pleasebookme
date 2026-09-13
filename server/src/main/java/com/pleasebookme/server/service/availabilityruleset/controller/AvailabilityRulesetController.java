package com.pleasebookme.server.service.availabilityruleset.controller;

import com.pleasebookme.server.service.availabilityruleset.service.AvailabilityRulesetService;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetRequest;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability-rulesets")
@RequiredArgsConstructor
public class AvailabilityRulesetController {
    private final AvailabilityRulesetService availabilityRulesetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityRulesetResponse createAvailabilityRuleset(
        @Valid @RequestBody AvailabilityRulesetRequest request
    ) {
        return availabilityRulesetService.createAvailabilityRuleset(request);
    }
}
