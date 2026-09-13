package com.pleasebookme.server.service.availabilityruleset.dto;

import com.pleasebookme.server.core.availability.dto.AvailabilityResponse;
import com.pleasebookme.server.core.schedule.dto.ScheduleResponse;

import java.util.List;

public record AvailabilityRulesetResponse(
    ScheduleResponse schedule,
    List<AvailabilityResponse> availabilities
) {
}
