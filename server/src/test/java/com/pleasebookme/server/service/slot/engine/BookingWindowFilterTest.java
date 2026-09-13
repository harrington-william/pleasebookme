package com.pleasebookme.server.service.slot.engine;

import com.pleasebookme.server.service.slot.dto.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingWindowFilterTest {
    private static final Instant NOW = Instant.parse("2026-09-21T10:00:00Z");

    private final BookingWindowFilter filter = new BookingWindowFilter();

    @Test
    void keepsOnlyCandidatesInsideNoticeAndAdvanceWindow() {
        TimeSlot pastCandidate = startingAt("2026-09-21T09:00:00Z");
        TimeSlot justInsideNotice = startingAt("2026-09-21T11:59:00Z");
        TimeSlot atNotice = startingAt("2026-09-21T12:00:00Z");
        TimeSlot pastNotice = startingAt("2026-09-21T12:01:00Z");
        TimeSlot beforeAdvance = startingAt("2026-09-22T09:59:00Z");
        TimeSlot atAdvance = startingAt("2026-09-22T10:00:00Z");
        TimeSlot pastAdvance = startingAt("2026-09-22T10:01:00Z");

        List<TimeSlot> kept = filter.filter(
            List.of(pastCandidate, justInsideNotice, atNotice, pastNotice, beforeAdvance, atAdvance, pastAdvance),
            120,
            1440,
            NOW
        );

        assertThat(kept).containsExactly(atNotice, pastNotice, beforeAdvance, atAdvance);
    }

    @Test
    void zeroNoticeStillDropsSlotsThatAlreadyStarted() {
        TimeSlot started = startingAt("2026-09-21T09:59:00Z");
        TimeSlot startingNow = startingAt("2026-09-21T10:00:00Z");

        List<TimeSlot> kept = filter.filter(List.of(started, startingNow), 0, 1440, NOW);

        assertThat(kept).containsExactly(startingNow);
    }

    @Test
    void zeroAdvanceKeepsNothingBeyondNow() {
        List<TimeSlot> kept = filter.filter(
            List.of(startingAt("2026-09-21T10:00:00Z"), startingAt("2026-09-21T10:30:00Z")),
            0,
            0,
            NOW
        );

        assertThat(kept).containsExactly(startingAt("2026-09-21T10:00:00Z"));
    }

    @Test
    void emptyInputYieldsEmptyOutput() {
        assertThat(filter.filter(List.of(), 60, 1440, NOW)).isEmpty();
    }

    private static TimeSlot startingAt(String start) {
        Instant slotStart = Instant.parse(start);
        return new TimeSlot(slotStart, slotStart.plus(30, ChronoUnit.MINUTES));
    }
}
