package com.example.starter.config;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds {@code classpath:db/demo} (Vietnamese sample data + demo accounts) to Flyway when
 * {@code app.demo-data=true}. Keep it false for real deployments.
 */
@Configuration
public class FlywayDemoDataConfig {

    @Bean
    FlywayConfigurationCustomizer demoDataLocation(AppProperties props) {
        return configuration -> {
            if (!props.demoData()) {
                return;
            }
            List<org.flywaydb.core.api.Location> locations = new ArrayList<>(Arrays.asList(configuration.getLocations()));
            locations.add(new org.flywaydb.core.api.Location("classpath:db/demo"));
            configuration.locations(locations.toArray(org.flywaydb.core.api.Location[]::new));
        };
    }
}
