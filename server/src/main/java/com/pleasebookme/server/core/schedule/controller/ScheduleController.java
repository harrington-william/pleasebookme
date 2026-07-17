package com.pleasebookme.server.core.schedule.controller;

import com.pleasebookme.server.core.schedule.dto.ScheduleRequest;
import com.pleasebookme.server.core.schedule.dto.ScheduleResponse;
import com.pleasebookme.server.core.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return ScheduleResponse.from(scheduleService.createSchedule(request));
    }

    @GetMapping("/{scheduleId}")
    public ScheduleResponse getSchedule(@PathVariable BigInteger scheduleId) {
        return ScheduleResponse.from(scheduleService.getScheduleById(scheduleId));
    }

    @GetMapping
    public List<ScheduleResponse> getSchedules() {
        return scheduleService.getAllSchedules().stream()
            .map(ScheduleResponse::from)
            .toList();
    }

    @PutMapping("/{scheduleId}")
    public ScheduleResponse updateSchedule(
        @PathVariable BigInteger scheduleId,
        @Valid @RequestBody ScheduleRequest request
    ) {
        return ScheduleResponse.from(scheduleService.updateSchedule(scheduleId, request));
    }

    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(@PathVariable BigInteger scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
    }
}
