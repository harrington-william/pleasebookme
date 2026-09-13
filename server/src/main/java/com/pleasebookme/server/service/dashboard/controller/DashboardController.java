package com.pleasebookme.server.service.dashboard.controller;

import com.pleasebookme.server.service.dashboard.dto.DashboardSummaryResponse;
import com.pleasebookme.server.service.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary(
        @RequestParam BigInteger organizationId
    ) {
        return dashboardService.getDashboardSummary(organizationId);
    }
}
