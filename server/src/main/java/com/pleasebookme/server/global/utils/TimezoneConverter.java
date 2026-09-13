package com.pleasebookme.server.global.utils;

import java.time.*;

public class TimezoneConverter {
    public static Instant toInstant(
        LocalDate date,
        LocalTime time,
        ZoneId zone
    ) {
        return LocalDateTime.of(date, time).atZone(zone).toInstant();
    }

    public static LocalDateTime toLocalDateTime(
        Instant instant,
        ZoneId zone
    ) {
        return instant.atZone(zone).toLocalDateTime();
    }
}
