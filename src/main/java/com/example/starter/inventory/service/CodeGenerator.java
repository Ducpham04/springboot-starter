package com.example.starter.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Human-friendly document numbers: {@code DH260105-0001} = prefix + yyMMdd + daily sequence.
 * Uses MySQL's atomic {@code LAST_INSERT_ID(expr)} counter trick (no table lock, safe under concurrency).
 */
@Component
@RequiredArgsConstructor
public class CodeGenerator {

    public static final String ORDER = "DH";
    public static final String RECEIPT = "PN";

    /** Business day boundaries follow Vietnam time. */
    public static final ZoneId ZONE = com.example.starter.common.util.DateRanges.ZONE;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyMMdd");

    private final JdbcTemplate jdbc;
    private final Clock clock;

    @Transactional(propagation = Propagation.MANDATORY)
    public String next(String prefix) {
        String key = prefix + LocalDate.now(clock.withZone(ZONE)).format(DAY);
        jdbc.update("INSERT INTO code_sequences (seq_key, value) VALUES (?, LAST_INSERT_ID(1)) "
                + "ON DUPLICATE KEY UPDATE value = LAST_INSERT_ID(value + 1)", key);
        Long value = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return "%s-%04d".formatted(key, value);
    }
}
