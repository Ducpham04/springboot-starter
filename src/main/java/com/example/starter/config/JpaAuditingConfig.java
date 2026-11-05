package com.example.starter.config;

import com.example.starter.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /** Fills createdBy/updatedBy with the email of the logged-in user, or "system". */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(SecurityUtils.currentUsername().orElse("system"));
    }
}
