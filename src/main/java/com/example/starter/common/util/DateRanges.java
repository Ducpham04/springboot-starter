package com.example.starter.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Converts business dates (Vietnam time) to UTC instants for queries. */
public final class DateRanges {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private DateRanges() {
    }

    public static Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant();
    }

    /** Inclusive end: last instant of the day. */
    public static Instant endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZONE).toInstant().minusNanos(1);
    }
}
