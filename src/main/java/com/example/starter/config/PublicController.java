package com.example.starter.config;

import com.example.starter.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RestController
@RequiredArgsConstructor
public class PublicController {

    private final AppProperties props;

    @Value("${spring.application.name}")
    private String appName;

    /** Lets the login page show demo accounts only when demo data is installed. */
    @GetMapping("/api/v1/public/app-info")
    public ApiResponse<AppInfo> appInfo() {
        return ApiResponse.ok(new AppInfo(appName, props.demoData(), props.auth().registrationEnabled()));
    }

    public record AppInfo(String name, boolean demo, boolean registrationEnabled) {
    }
}
