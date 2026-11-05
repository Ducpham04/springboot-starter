package com.example.starter.dashboard;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.dashboard.dto.DashboardResponse;
import com.example.starter.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "KPIs, 14-day revenue, top products, low stock, recent orders")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @GetMapping
    public ApiResponse<DashboardResponse> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }
}
