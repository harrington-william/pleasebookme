package com.pleasebookme.server.service.slot.engine;

import com.pleasebookme.server.global.utils.TimezoneConverter;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class SlotGenerator {

    public List<TimeSlot> generateSlots(
        LocalDate date,
        LocalTime availabilityStart,
        LocalTime availabilityEnd,
        int durationMinutes,
        int intervalMinutes,
        ZoneId zone
    ) {
        // core.booking_policies has no CHECK on slot_interval/default_duration. A zero
        // interval would never advance the loop below and hang the request thread, a
        // negative duration would never terminate it — fail loudly before either.
        if (durationMinutes <= 0 || intervalMinutes <= 0) {
            throw new IllegalArgumentException(
                "Slot generation requires positive duration and interval, got duration="
                    + durationMinutes + " interval=" + intervalMinutes
            );
        }

        Instant windowEnd = TimezoneConverter.toInstant(date, availabilityEnd, zone);
        Instant slotStart = TimezoneConverter.toInstant(date, availabilityStart, zone);
        List<TimeSlot> slots = new ArrayList<>();

        while (!slotStart.plus(durationMinutes, ChronoUnit.MINUTES).isAfter(windowEnd)) {
            Instant slotEnd = slotStart.plus(durationMinutes, ChronoUnit.MINUTES);
            slots.add(new TimeSlot(slotStart, slotEnd));
            slotStart = slotStart.plus(intervalMinutes, ChronoUnit.MINUTES);
        }

        return slots;
    }
}
