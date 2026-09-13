package com.pleasebookme.server.service.slot.engine;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BufferCalculatorTest {
    private final BufferCalculator bufferCalculator = new BufferCalculator();

    @Test
    void blockedStartMovesBackwardByBeforeBufferMinutes() {
        Instant start = Instant.parse("2026-09-21T10:00:00Z");

        assertThat(bufferCalculator.calculateBlockedStart(start, 15))
            .isEqualTo(Instant.parse("2026-09-21T09:45:00Z"));
    }

    @Test
    void blockedEndMovesForwardByAfterBufferMinutes() {
        Instant end = Instant.parse("2026-09-21T10:30:00Z");

        assertThat(bufferCalculator.calculateBlockedEnd(end, 15))
            .isEqualTo(Instant.parse("2026-09-21T10:45:00Z"));
    }

    @Test
    void zeroBufferLeavesInstantUnchanged() {
        Instant instant = Instant.parse("2026-09-21T10:00:00Z");

        assertThat(bufferCalculator.calculateBlockedStart(instant, 0)).isEqualTo(instant);
        assertThat(bufferCalculator.calculateBlockedEnd(instant, 0)).isEqualTo(instant);
    }
}
