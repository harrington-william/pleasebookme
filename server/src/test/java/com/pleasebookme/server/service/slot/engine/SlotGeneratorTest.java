package com.pleasebookme.server.service.slot.engine;

import com.pleasebookme.server.service.slot.dto.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotGeneratorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 9, 21);
    private static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    private final SlotGenerator slotGenerator = new SlotGenerator();

    @Test
    void emitsOneSlotPerIntervalAcrossTheWindow() {
        List<TimeSlot> slots = slotGenerator.generateSlots(
            DATE,
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            30,
            30,
            ZoneOffset.UTC
        );

        assertThat(slots).hasSize(16);
        assertThat(slots.getFirst()).isEqualTo(
            new TimeSlot(Instant.parse("2026-09-21T09:00:00Z"), Instant.parse("2026-09-21T09:30:00Z"))
        );
        assertThat(slots.getLast()).isEqualTo(
            new TimeSlot(Instant.parse("2026-09-21T16:30:00Z"), Instant.parse("2026-09-21T17:00:00Z"))
        );
    }

    @Test
    void doesNotEmitASlotWhoseEndPassesTheWindowEnd() {
        List<TimeSlot> slots = slotGenerator.generateSlots(
            DATE,
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            45,
            15,
            ZoneOffset.UTC
        );

        assertThat(slots).containsExactly(
            new TimeSlot(Instant.parse("2026-09-21T09:00:00Z"), Instant.parse("2026-09-21T09:45:00Z")),
            new TimeSlot(Instant.parse("2026-09-21T09:15:00Z"), Instant.parse("2026-09-21T10:00:00Z"))
        );
    }

    @Test
    void emptyOrInvertedWindowYieldsNoSlots() {
        assertThat(slotGenerator.generateSlots(DATE, LocalTime.of(9, 0), LocalTime.of(9, 0), 30, 30, ZoneOffset.UTC))
            .isEmpty();
        assertThat(slotGenerator.generateSlots(DATE, LocalTime.of(17, 0), LocalTime.of(9, 0), 30, 30, ZoneOffset.UTC))
            .isEmpty();
    }

    @Test
    void springForwardDayHasOneHourFewerSlots() {
        // Australia/Sydney 2026-10-04: 02:00 AEST jumps to 03:00 AEDT, so 00:00-06:00 local is five real hours.
        List<TimeSlot> slots = slotGenerator.generateSlots(
            LocalDate.of(2026, 10, 4),
            LocalTime.MIDNIGHT,
            LocalTime.of(6, 0),
            30,
            30,
            SYDNEY
        );

        assertThat(slots).hasSize(10);
    }

    @Test
    void fallBackDayHasOneHourMoreSlots() {
        // Australia/Sydney 2026-04-05: 03:00 AEDT falls back to 02:00 AEST, so 00:00-06:00 local is seven real hours.
        List<TimeSlot> slots = slotGenerator.generateSlots(
            LocalDate.of(2026, 4, 5),
            LocalTime.MIDNIGHT,
            LocalTime.of(6, 0),
            30,
            30,
            SYDNEY
        );

        assertThat(slots).hasSize(14);
    }

    @Test
    @Timeout(1)
    void zeroIntervalIsRejectedBeforeTheLoop() {
        assertThatThrownBy(() ->
            slotGenerator.generateSlots(DATE, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 0, ZoneOffset.UTC)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("interval=0");
    }

    @Test
    @Timeout(1)
    void zeroDurationIsRejectedBeforeTheLoop() {
        assertThatThrownBy(() ->
            slotGenerator.generateSlots(DATE, LocalTime.of(9, 0), LocalTime.of(17, 0), 0, 30, ZoneOffset.UTC)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("duration=0");
    }

    @Test
    @Timeout(1)
    void negativeIntervalIsRejectedBeforeTheLoop() {
        assertThatThrownBy(() ->
            slotGenerator.generateSlots(DATE, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, -5, ZoneOffset.UTC)
        )
            .isInstanceOf(IllegalArgumentException.class);
    }
}
