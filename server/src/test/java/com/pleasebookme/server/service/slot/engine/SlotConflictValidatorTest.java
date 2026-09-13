package com.pleasebookme.server.service.slot.engine;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;
import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlotConflictValidatorTest {
    private static final TimeSlot TEN_TO_TEN_THIRTY = slot("10:00", "10:30");

    private final SlotConflictValidator validator = new SlotConflictValidator(new BufferCalculator());

    @Test
    void touchingBookingWithNoBufferDoesNotConflict() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(booking("10:30", "11:00")),
            List.of(),
            List.of(),
            0,
            0
        );

        assertThat(available).isTrue();
    }

    @Test
    void touchingBookingWithBeforeBufferConflicts() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(booking("10:30", "11:00")),
            List.of(),
            List.of(),
            15,
            0
        );

        assertThat(available).isFalse();
    }

    @Test
    void bookingBeyondTheBeforeBufferDoesNotConflict() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(booking("11:00", "11:30")),
            List.of(),
            List.of(),
            15,
            15
        );

        assertThat(available).isTrue();
    }

    @Test
    void afterBufferOfAnEarlierBookingConflicts() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(booking("09:15", "09:50")),
            List.of(),
            List.of(),
            0,
            15
        );

        assertThat(available).isFalse();
    }

    @Test
    void overlappingBookingConflicts() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(booking("10:15", "10:45")),
            List.of(),
            List.of(),
            0,
            0
        );

        assertThat(available).isFalse();
    }

    @Test
    void holdIsBufferedLikeABooking() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(),
            List.of(hold("10:30", "11:00")),
            List.of(),
            15,
            0
        );

        assertThat(available).isFalse();
    }

    @Test
    void touchingHoldWithNoBufferDoesNotConflict() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(),
            List.of(hold("10:30", "11:00")),
            List.of(),
            0,
            0
        );

        assertThat(available).isTrue();
    }

    @Test
    void outOfOfficeIsNotBuffered() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(),
            List.of(),
            List.of(outOfOffice("10:30", "11:00")),
            15,
            15
        );

        assertThat(available).isTrue();
    }

    @Test
    void overlappingOutOfOfficeConflicts() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(),
            List.of(),
            List.of(outOfOffice("10:00", "12:00")),
            0,
            0
        );

        assertThat(available).isFalse();
    }

    @Test
    void crossMidnightOutOfOfficeIsAPlainOverlap() {
        TimeSlot midnightSlot = new TimeSlot(
            Instant.parse("2026-09-21T00:00:00Z"),
            Instant.parse("2026-09-21T00:30:00Z")
        );
        OutOfOfficeEntity outOfOffice = OutOfOfficeEntity.builder()
            .startTime(Instant.parse("2026-09-20T23:00:00Z"))
            .endTime(Instant.parse("2026-09-21T01:00:00Z"))
            .build();

        boolean available = validator.isSlotAvailable(
            midnightSlot,
            List.of(),
            List.of(),
            List.of(outOfOffice),
            0,
            0
        );

        assertThat(available).isFalse();
    }

    @Test
    void noConflictsMeansAvailable() {
        boolean available = validator.isSlotAvailable(
            TEN_TO_TEN_THIRTY,
            List.of(),
            List.of(),
            List.of(),
            15,
            15
        );

        assertThat(available).isTrue();
    }

    private static TimeSlot slot(
        String start,
        String end
    ) {
        return new TimeSlot(at(start), at(end));
    }

    private static BookingEntity booking(
        String start,
        String end
    ) {
        return BookingEntity.builder()
            .startTime(at(start))
            .endTime(at(end))
            .build();
    }

    private static SelectedSlotEntity hold(
        String start,
        String end
    ) {
        return SelectedSlotEntity.builder()
            .slotStart(at(start))
            .slotEnd(at(end))
            .build();
    }

    private static OutOfOfficeEntity outOfOffice(
        String start,
        String end
    ) {
        return OutOfOfficeEntity.builder()
            .startTime(at(start))
            .endTime(at(end))
            .build();
    }

    private static Instant at(String hhmm) {
        return Instant.parse("2026-09-21T" + hhmm + ":00Z");
    }
}
