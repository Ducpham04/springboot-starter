package com.example.starter;

import com.example.starter.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

/**
 * Run the app locally against a throw-away MySQL container (no docker compose needed):
 * {@code ./mvnw spring-boot:test-run}
 */
public class TestStarterApplication {

    public static void main(String[] args) {
        SpringApplication.from(StarterApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
