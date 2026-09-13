package com.pleasebookme.server.service.slot.engine;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;
import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotConflictValidator {
    private final BufferCalculator bufferCalculator;

    public boolean isSlotAvailable(
        TimeSlot slot,
        List<BookingEntity> bookings,
        List<SelectedSlotEntity> selectedSlots,
        List<OutOfOfficeEntity> outOfOfficePeriods,
        int beforeBufferMinutes,
        int afterBufferMinutes
    ) {
        return !conflictsWithBookings(slot, bookings, beforeBufferMinutes, afterBufferMinutes)
            && !conflictsWithSelectedSlots(slot, selectedSlots, beforeBufferMinutes, afterBufferMinutes)
            && !conflictsWithOutOfOffice(slot, outOfOfficePeriods);
    }

    private boolean conflictsWithBookings(
        TimeSlot slot,
        List<BookingEntity> bookings,
        int beforeBufferMinutes,
        int afterBufferMinutes
    ) {
        for (BookingEntity booking : bookings) {
            if (overlapsBuffered(
                slot,
                booking.getStartTime(),
                booking.getEndTime(),
                beforeBufferMinutes,
                afterBufferMinutes
            )) {
                return true;
            }
        }

        return false;
    }

    // A hold is a booking-in-waiting. Compared unbuffered, a candidate that passes now
    // would conflict the moment the hold converts, so holds get the same buffers as bookings.
    private boolean conflictsWithSelectedSlots(
        TimeSlot slot,
        List<SelectedSlotEntity> selectedSlots,
        int beforeBufferMinutes,
        int afterBufferMinutes
    ) {
        for (SelectedSlotEntity selectedSlot : selectedSlots) {
            if (overlapsBuffered(
                slot,
                selectedSlot.getSlotStart(),
                selectedSlot.getSlotEnd(),
                beforeBufferMinutes,
                afterBufferMinutes
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean conflictsWithOutOfOffice(
        TimeSlot slot,
        List<OutOfOfficeEntity> outOfOfficePeriods
    ) {
        for (OutOfOfficeEntity outOfOffice : outOfOfficePeriods) {
            if (overlaps(
                slot,
                outOfOffice.getStartTime(),
                outOfOffice.getEndTime()
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean overlapsBuffered(
        TimeSlot slot,
        Instant start,
        Instant end,
        int beforeBufferMinutes,
        int afterBufferMinutes
    ) {
        Instant blockedStart = bufferCalculator.calculateBlockedStart(start, beforeBufferMinutes);
        Instant blockedEnd = bufferCalculator.calculateBlockedEnd(end, afterBufferMinutes);

        return overlaps(slot, blockedStart, blockedEnd);
    }

    private boolean overlaps(
        TimeSlot slot,
        Instant blockedStart,
        Instant blockedEnd
    ) {
        return slot.slotStart().isBefore(blockedEnd)
            && slot.slotEnd().isAfter(blockedStart);
    }
}
