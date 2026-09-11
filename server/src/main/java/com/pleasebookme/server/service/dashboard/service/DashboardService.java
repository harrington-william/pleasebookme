package com.pleasebookme.server.service.dashboard.service;

import com.pleasebookme.server.service.dashboard.dto.DashboardSummaryResponse;

import java.math.BigInteger;

public interface DashboardService {
    DashboardSummaryResponse getDashboardSummary(BigInteger organizationId);
}
