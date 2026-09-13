package com.pleasebookme.server.service.slot;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.repository.BookingRepository;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.exception.BookingPolicyNotFoundException;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.core.outofoffice.entity.OutOfOfficeEntity;
import com.pleasebookme.server.core.outofoffice.repository.OutOfOfficeRepository;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.selectedslot.entity.SelectedSlotEntity;
import com.pleasebookme.server.core.selectedslot.repository.SelectedSlotRepository;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import com.pleasebookme.server.service.slot.engine.BookingWindowFilter;
import com.pleasebookme.server.service.slot.engine.BufferCalculator;
import com.pleasebookme.server.service.slot.engine.SlotConflictValidator;
import com.pleasebookme.server.service.slot.engine.SlotGenerator;
import com.pleasebookme.server.service.slot.service.impl.SlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {
    private static final BigInteger HOST_USER_ID = BigInteger.valueOf(42);
    private static final BigInteger SCHEDULE_ID = BigInteger.valueOf(11);
    private static final BigInteger SERVICE_ID = BigInteger.valueOf(15);
    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Australia/Sydney");
    private static final int BEFORE_BUFFER = 15;
    private static final int AFTER_BUFFER = 10;

    // ISO weekday numbers, Monday=1..Sunday=7 — the same values DayOfWeek.getValue() returns.
    private static final Integer[] SUNDAY_ONLY = {7};
    private static final Integer[] MONDAY_ONLY = {1};

    @Mock private ServiceRepository serviceRepository;
    @Mock private BookingPolicyRepository bookingPolicyRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private SelectedSlotRepository selectedSlotRepository;
    @Mock private OutOfOfficeRepository outOfOfficeRepository;

    private SlotServiceImpl slotService;
    private ServiceEntity service;
    private ScheduleEntity schedule;
    private BookingPolicyEntity policy;
    private LocalDate nextSunday;
    private LocalDate nextMonday;

    @BeforeEach
    void setUp() {
        slotService = new SlotServiceImpl(
            serviceRepository,
            bookingPolicyRepository,
            availabilityRepository,
            bookingRepository,
            selectedSlotRepository,
            outOfOfficeRepository,
            new SlotGenerator(),
            new BookingWindowFilter(),
            new SlotConflictValidator(new BufferCalculator())
        );

        UserEntity host = UserEntity.builder().username("host").build();
        host.setUserId(HOST_USER_ID);

        schedule = ScheduleEntity.builder()
            .user(host)
            .title("Working Hours")
            .timezone(SCHEDULE_ZONE.getId())
            .build();
        schedule.setScheduleId(SCHEDULE_ID);

        // Service timezone deliberately differs from the schedule's so the test can tell which one won.
        service = ServiceEntity.builder()
            .title("Haircut")
            .slug("haircut")
            .user(host)
            .schedule(schedule)
            .timezone("Asia/Saigon")
            .build();
        service.setServiceId(SERVICE_ID);

        // Zero notice and a two-week advance window keep Instant.now() from trimming next week's slots.
        policy = BookingPolicyEntity.builder()
            .service(service)
            .defaultDuration(30)
            .slotInterval(30)
            .beforeBuffer(BEFORE_BUFFER)
            .afterBuffer(AFTER_BUFFER)
            .minimumNotice(0)
            .maximumAdvanceBooking(20_000)
            .bookingWindowType("ROLLING")
            .capacity(1)
            .build();

        LocalDate today = LocalDate.now(SCHEDULE_ZONE);
        nextSunday = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    @Test
    void unknownServiceThrowsBeforeAnyOtherLookup() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.getAvailableSlots(SERVICE_ID, nextMonday))
            .isInstanceOf(ServiceNotFoundException.class)
            .hasMessageContaining(SERVICE_ID.toString());

        verifyNoInteractions(
            bookingPolicyRepository,
            availabilityRepository,
            bookingRepository,
            selectedSlotRepository,
            outOfOfficeRepository
        );
    }

    @Test
    void missingPolicyThrowsBeforeAvailabilityOrConflictLookups() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(bookingPolicyRepository.findByServiceServiceId(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.getAvailableSlots(SERVICE_ID, nextMonday))
            .isInstanceOf(BookingPolicyNotFoundException.class)
            .hasMessageContaining(SERVICE_ID.toString());

        verifyNoInteractions(availabilityRepository, bookingRepository, selectedSlotRepository, outOfOfficeRepository);
    }

    @Test
    void sundayAvailabilityMatchesASundayDate() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(SUNDAY_ONLY, LocalTime.of(10, 0), LocalTime.of(12, 0)));
        stubNoConflicts();

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextSunday);

        assertThat(response.slots()).hasSize(4);
        assertThat(response.slots().getFirst().slotStart())
            .isEqualTo(nextSunday.atTime(10, 0).atZone(SCHEDULE_ZONE).toInstant());
    }

    @Test
    void zeroIsNotAWeekdayAndNeverMatches() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(new Integer[] {0}, LocalTime.of(10, 0), LocalTime.of(12, 0)));

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextSunday);

        assertThat(response.slots()).isEmpty();
        verifyNoInteractions(bookingRepository, selectedSlotRepository, outOfOfficeRepository);
    }

    @Test
    void sundayAvailabilityDoesNotMatchAMondayDate() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(SUNDAY_ONLY, LocalTime.of(10, 0), LocalTime.of(12, 0)));

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        assertThat(response.slots()).isEmpty();
        verifyNoInteractions(bookingRepository, selectedSlotRepository, outOfOfficeRepository);
    }

    @Test
    void mondayAvailabilityMatchesAMondayDate() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(MONDAY_ONLY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        stubNoConflicts();

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        assertThat(response.slots()).hasSize(16);
    }

    @Test
    void closedDayReturnsEmptyListWithoutConflictQueries() {
        stubServiceAndPolicy();
        stubAvailabilities();

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        assertThat(response.serviceId()).isEqualTo(SERVICE_ID);
        assertThat(response.date()).isEqualTo(nextMonday);
        assertThat(response.timezone()).isEqualTo(SCHEDULE_ZONE.getId());
        assertThat(response.slots()).isEmpty();
        verifyNoInteractions(bookingRepository, selectedSlotRepository, outOfOfficeRepository);
    }

    @Test
    void conflictFindersReceiveHostUserAndBufferWidenedWindow() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(MONDAY_ONLY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        stubNoConflicts();
        Instant beforeCall = Instant.now();

        slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        Instant afterCall = Instant.now();
        Instant dayStart = nextMonday.atStartOfDay(SCHEDULE_ZONE).toInstant();
        Instant dayEnd = nextMonday.plusDays(1).atStartOfDay(SCHEDULE_ZONE).toInstant();
        Instant expectedLoadFrom = dayStart.minus(AFTER_BUFFER, ChronoUnit.MINUTES);
        Instant expectedLoadTo = dayEnd.plus(BEFORE_BUFFER, ChronoUnit.MINUTES);

        ArgumentCaptor<Instant> now = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> holdLoadTo = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> holdLoadFrom = ArgumentCaptor.forClass(Instant.class);
        verify(selectedSlotRepository).findByServiceUserUserIdAndReleaseAtAfterAndSlotStartBeforeAndSlotEndAfter(
            eq(HOST_USER_ID),
            now.capture(),
            holdLoadTo.capture(),
            holdLoadFrom.capture()
        );
        assertThat(now.getValue()).isBetween(beforeCall, afterCall);
        assertThat(holdLoadTo.getValue()).isEqualTo(expectedLoadTo);
        assertThat(holdLoadFrom.getValue()).isEqualTo(expectedLoadFrom);

        verify(outOfOfficeRepository).findByUserUserIdAndStartTimeBeforeAndEndTimeAfter(
            HOST_USER_ID,
            dayEnd,
            dayStart
        );
    }

    @Test
    void bookingSpecificationIsRequestedOnce() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(MONDAY_ONLY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        stubNoConflicts();

        slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        verify(bookingRepository).findAll(any(Specification.class));
    }

    @Test
    void bookingsHoldsAndOutOfOfficeAllRemoveSlots() {
        stubServiceAndPolicy();
        stubAvailabilities(availability(MONDAY_ONLY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        // Booking 10:00-10:30 with 15/10 buffers removes 09:30, 10:00, 10:30.
        when(bookingRepository.findAll(any(Specification.class)))
            .thenReturn(List.of(booking(10, 0, 10, 30)));
        // Hold 13:00-13:30 (buffered like a booking) removes 12:30, 13:00, 13:30.
        when(selectedSlotRepository.findByServiceUserUserIdAndReleaseAtAfterAndSlotStartBeforeAndSlotEndAfter(
            eq(HOST_USER_ID), any(), any(), any()
        )).thenReturn(List.of(hold(13, 0, 13, 30)));
        // OOO 15:00-16:00 (unbuffered) removes 15:00, 15:30 only.
        when(outOfOfficeRepository.findByUserUserIdAndStartTimeBeforeAndEndTimeAfter(
            eq(HOST_USER_ID), any(), any()
        )).thenReturn(List.of(outOfOffice(15, 0, 16, 0)));

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        assertThat(response.slots()).hasSize(8);
        assertThat(response.slots())
            .extracting(TimeSlot::slotStart)
            .containsExactly(
                localInstant(nextMonday, 9, 0),
                localInstant(nextMonday, 11, 0),
                localInstant(nextMonday, 11, 30),
                localInstant(nextMonday, 12, 0),
                localInstant(nextMonday, 14, 0),
                localInstant(nextMonday, 14, 30),
                localInstant(nextMonday, 16, 0),
                localInstant(nextMonday, 16, 30)
            );
    }

    @Test
    void overlappingAvailabilityRowsYieldSortedDistinctSlots() {
        stubServiceAndPolicy();
        stubAvailabilities(
            availability(MONDAY_ONLY, LocalTime.of(12, 0), LocalTime.of(17, 0)),
            availability(MONDAY_ONLY, LocalTime.of(9, 0), LocalTime.of(13, 0))
        );
        stubNoConflicts();

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        assertThat(response.slots()).hasSize(16);
        assertThat(response.slots()).doesNotHaveDuplicates();
        assertThat(response.slots())
            .extracting(TimeSlot::slotStart)
            .isSorted();
    }

    @Test
    void responseTimezoneComesFromTheScheduleNotTheService() {
        stubServiceAndPolicy();
        stubAvailabilities();

        AvailableSlotsResponse response = slotService.getAvailableSlots(SERVICE_ID, nextMonday);

        assertThat(response.timezone()).isEqualTo("Australia/Sydney");
        assertThat(response.timezone()).isNotEqualTo(service.getTimezone());
    }

    @Test
    void blockingStatusesAreExactlyPendingAcceptedAndAwaitingHost() throws Exception {
        Field field = SlotServiceImpl.class.getDeclaredField("BLOCKING_STATUSES");
        field.setAccessible(true);

        assertThat(Modifier.isStatic(field.getModifiers())).isTrue();
        assertThat(Modifier.isFinal(field.getModifiers())).isTrue();
        @SuppressWarnings("unchecked")
        Set<BookingStatus> blocking = (Set<BookingStatus>) field.get(null);
        assertThat(blocking).containsExactlyInAnyOrder(
            BookingStatus.PENDING,
            BookingStatus.ACCEPTED,
            BookingStatus.AWAITING_HOST
        );
    }

    private void stubServiceAndPolicy() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(bookingPolicyRepository.findByServiceServiceId(SERVICE_ID)).thenReturn(Optional.of(policy));
    }

    private void stubAvailabilities(AvailabilityEntity... availabilities) {
        when(availabilityRepository.findByScheduleScheduleId(SCHEDULE_ID))
            .thenReturn(List.of(availabilities));
    }

    private void stubNoConflicts() {
        when(bookingRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(selectedSlotRepository.findByServiceUserUserIdAndReleaseAtAfterAndSlotStartBeforeAndSlotEndAfter(
            eq(HOST_USER_ID), any(), any(), any()
        )).thenReturn(List.of());
        when(outOfOfficeRepository.findByUserUserIdAndStartTimeBeforeAndEndTimeAfter(
            eq(HOST_USER_ID), any(), any()
        )).thenReturn(List.of());
    }

    private AvailabilityEntity availability(
        Integer[] days,
        LocalTime start,
        LocalTime end
    ) {
        return AvailabilityEntity.builder()
            .schedule(schedule)
            .days(days)
            .startTime(start)
            .endTime(end)
            .build();
    }

    private BookingEntity booking(
        int startHour,
        int startMinute,
        int endHour,
        int endMinute
    ) {
        return BookingEntity.builder()
            .startTime(localInstant(nextMonday, startHour, startMinute))
            .endTime(localInstant(nextMonday, endHour, endMinute))
            .build();
    }

    private SelectedSlotEntity hold(
        int startHour,
        int startMinute,
        int endHour,
        int endMinute
    ) {
        return SelectedSlotEntity.builder()
            .slotStart(localInstant(nextMonday, startHour, startMinute))
            .slotEnd(localInstant(nextMonday, endHour, endMinute))
            .build();
    }

    private OutOfOfficeEntity outOfOffice(
        int startHour,
        int startMinute,
        int endHour,
        int endMinute
    ) {
        return OutOfOfficeEntity.builder()
            .startTime(localInstant(nextMonday, startHour, startMinute))
            .endTime(localInstant(nextMonday, endHour, endMinute))
            .build();
    }

    private static Instant localInstant(
        LocalDate date,
        int hour,
        int minute
    ) {
        return date.atTime(hour, minute).atZone(SCHEDULE_ZONE).toInstant();
    }
}
