package com.example.starter.config;

import com.example.starter.user.entity.Role;
import com.example.starter.user.entity.User;
import com.example.starter.user.repository.RoleRepository;
import com.example.starter.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Creates the bootstrap admin from {@code app.admin.*} (env APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD)
 * when it does not exist yet. Leave the variables empty to disable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final AppProperties props;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = props.admin();
        if (admin == null || !StringUtils.hasText(admin.email()) || !StringUtils.hasText(admin.password())) {
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(admin.email())) {
            return;
        }
        Role adminRole = roleRepository.findByCode(Role.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Role ADMIN missing - check Flyway migrations"));
        User user = new User();
        user.setEmail(admin.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(admin.password()));
        user.setFullName(StringUtils.hasText(admin.fullName()) ? admin.fullName() : "Administrator");
        user.setEnabled(true);
        user.getRoles().add(adminRole);
        userRepository.save(user);
        log.info("Bootstrap admin account created: {}", user.getEmail());
    }
}
