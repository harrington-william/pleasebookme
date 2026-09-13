package com.pleasebookme.server.service.slot.engine;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class BufferCalculator {

    public Instant calculateBlockedStart(
        Instant bookingStart,
        int beforeBufferMinutes
    ) {
        return bookingStart.minus(beforeBufferMinutes, ChronoUnit.MINUTES);
    }

    public Instant calculateBlockedEnd(
        Instant bookingEnd,
        int afterBufferMinutes
    ) {
        return bookingEnd.plus(afterBufferMinutes, ChronoUnit.MINUTES);
    }
}
