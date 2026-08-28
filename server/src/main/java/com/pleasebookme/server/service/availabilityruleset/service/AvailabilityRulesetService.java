package com.pleasebookme.server.service.availabilityruleset.service;

import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetRequest;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetResponse;

public interface AvailabilityRulesetService {
    AvailabilityRulesetResponse createAvailabilityRuleset(AvailabilityRulesetRequest request);
}
