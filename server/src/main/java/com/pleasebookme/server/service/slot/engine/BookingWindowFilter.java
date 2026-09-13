package com.pleasebookme.server.service.slot.engine;

import com.pleasebookme.server.service.slot.dto.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BookingWindowFilter {

    // booking_window_type is deliberately not consulted
    // FIXED and ROLLING are now treated in the same way
    public List<TimeSlot> filter(
        List<TimeSlot> candidates,
        int minimumNotice,
        int maximumAdvanceBooking,
        Instant now
    ) {
        Instant earliestStart = now.plus(minimumNotice, ChronoUnit.MINUTES);
        Instant latestStart = now.plus(maximumAdvanceBooking, ChronoUnit.MINUTES);

        return candidates.stream()
            .filter(candidate -> !candidate.slotStart().isBefore(earliestStart))
            .filter(candidate -> !candidate.slotStart().isAfter(latestStart))
            .toList();
    }
}
