package com.pleasebookme.server.service.widget.barbershop;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.attendee.entity.AttendeeEntity;
import com.pleasebookme.server.core.attendee.repository.AttendeeRepository;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.booking.domain.entity.BookingEntity;
import com.pleasebookme.server.core.booking.domain.repository.BookingRepository;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.enums.BookingStatus;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.global.enums.WeekStart;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.service.slot.dto.AvailableSlotsResponse;
import com.pleasebookme.server.service.slot.dto.TimeSlot;
import com.pleasebookme.server.service.slot.service.SlotService;
import com.pleasebookme.server.service.booking.barbershop.dto.WidgetBookingRequest;
import com.pleasebookme.server.service.booking.barbershop.exception.SlotUnavailableException;
import com.pleasebookme.server.service.booking.barbershop.service.impl.BarbershopBookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigInteger;
import com.pleasebookme.server.service.widget.ServedOrganization;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BarbershopBookingServiceImplTest {
    @Mock private ServiceRepository serviceRepository;
    @Mock private BookingPolicyRepository bookingPolicyRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AttendeeRepository attendeeRepository;
    @Mock private SlotService slotService;

    private BarbershopBookingServiceImpl bookingService;
    private OrganizationEntity organization;
    private ServedOrganization served;
    private UserEntity host;
    private ScheduleEntity schedule;
    private ServiceEntity service;
    private BookingPolicyEntity policy;

    @BeforeEach
    void setUp() {
        organization = OrganizationEntity.builder()
            .organizationId(BigInteger.ONE)
            .name("Acme")
            .slug("acme")
            .timezone("Europe/London")
            .weekStart(WeekStart.MONDAY)
            .build();
        served = new ServedOrganization(organization, "GENERAL");
        host = UserEntity.builder().userId(BigInteger.TWO).build();
        schedule = ScheduleEntity.builder()
            .scheduleId(BigInteger.valueOf(3))
            .user(host)
            .title("Hours")
            .timezone("Australia/Sydney")
            .build();
        service = ServiceEntity.builder()
            .serviceId(BigInteger.TEN)
            .organization(organization)
            .user(host)
            .schedule(schedule)
            .slug("consultation")
            .title("Consultation")
            .timezone("America/New_York")
            .location("Studio 1")
            .minPrice(java.math.BigDecimal.TEN)
            .maxPrice(java.math.BigDecimal.TEN)
            .currency(Currency.AUD)
            .successRedirectUrl("https://example.com")
            .build();
        policy = BookingPolicyEntity.builder()
            .bookingPolicyId(BigInteger.ONE)
            .service(service)
            .defaultDuration(30)
            .minimumNotice(60)
            .maximumAdvanceBooking(43200)
            .autoConfirm(true)
            .build();

        bookingService = new BarbershopBookingServiceImpl(
            serviceRepository,
            bookingPolicyRepository,
            availabilityRepository,
            userRepository,
            bookingRepository,
            attendeeRepository,
            slotService
        );
        when(serviceRepository.findByOrganizationOrganizationIdAndSlug(
            BigInteger.ONE,
            "consultation"
        )).thenReturn(Optional.of(service));
        when(bookingPolicyRepository.findByServiceServiceId(BigInteger.TEN))
            .thenReturn(Optional.of(policy));
    }

    @Test
    void getOrganization_filtersServicesWithoutPoliciesUsingOneInQuery() {
        ServiceEntity hidden = ServiceEntity.builder()
            .serviceId(BigInteger.valueOf(11))
            .title("Hidden")
            .slug("hidden")
            .build();
        when(serviceRepository.findByOrganizationOrganizationId(BigInteger.ONE))
            .thenReturn(List.of(hidden, service));
        when(bookingPolicyRepository.findByServiceServiceIdIn(
            List.of(BigInteger.valueOf(11), BigInteger.TEN)
        )).thenReturn(List.of(policy));

        var response = bookingService.getOrganization(served);

        assertThat(response.weekStart()).isEqualTo(WeekStart.MONDAY);
        assertThat(response.services()).singleElement().satisfies(summary -> {
            assertThat(summary.slug()).isEqualTo("consultation");
            assertThat(summary.durationMinutes()).isEqualTo(30);
            assertThat(summary.autoConfirm()).isTrue();
        });
        verify(bookingPolicyRepository).findByServiceServiceIdIn(
            List.of(BigInteger.valueOf(11), BigInteger.TEN)
        );
    }

    @Test
    void getService_usesScheduleZoneAndSortedDistinctAvailabilityDays() {
        when(availabilityRepository.findByScheduleScheduleId(BigInteger.valueOf(3)))
            .thenReturn(List.of(
                AvailabilityEntity.builder().days(new Integer[]{5, 1, 3}).build(),
                AvailabilityEntity.builder().days(new Integer[]{3, 2}).build()
            ));

        var response = bookingService.getService(served, "consultation");

        assertThat(response.scheduleTimezone()).isEqualTo("Australia/Sydney");
        assertThat(response.availableWeekdays()).containsExactly(1, 2, 3, 5);
        assertThat(response.successRedirectUrl()).isEqualTo("https://example.com");
    }

    @Test
    void getService_missingPolicyIsReportedAsMissingService() {
        when(bookingPolicyRepository.findByServiceServiceId(BigInteger.TEN))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getService(served, "consultation"))
            .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void getSlots_returnsEngineResponseUnchanged() {
        LocalDate date = LocalDate.of(2026, 9, 24);
        AvailableSlotsResponse expected = new AvailableSlotsResponse(
            BigInteger.TEN,
            date,
            "Australia/Sydney",
            List.of()
        );
        when(slotService.getAvailableSlots(BigInteger.TEN, date)).thenReturn(expected);

        assertThat(bookingService.getSlots(served, "consultation", date))
            .isSameAs(expected);
    }

    @Test
    void createBooking_locksBeforeEngineAndPersistsValidatedSlot() {
        Instant start = Instant.parse("2026-09-23T23:30:00Z");
        Instant end = start.plusSeconds(1800);
        when(userRepository.findLockedByUserId(BigInteger.TWO)).thenReturn(Optional.of(host));
        when(slotService.getAvailableSlots(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24)
        )).thenReturn(new AvailableSlotsResponse(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24),
            "Australia/Sydney",
            List.of(new TimeSlot(start, end))
        ));
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(BookingEntity.class)))
            .thenAnswer(invocation -> {
                BookingEntity booking = invocation.getArgument(0);
                booking.setBookingUid(UUID.randomUUID());
                return booking;
            });

        var response = bookingService.createBooking(
            served,
            "consultation",
            request(start)
        );

        InOrder ordering = inOrder(userRepository, slotService);
        ordering.verify(userRepository).findLockedByUserId(BigInteger.TWO);
        ordering.verify(slotService).getAvailableSlots(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24)
        );
        ArgumentCaptor<BookingEntity> bookingCaptor = ArgumentCaptor.forClass(BookingEntity.class);
        ArgumentCaptor<AttendeeEntity> attendeeCaptor = ArgumentCaptor.forClass(AttendeeEntity.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        verify(attendeeRepository).save(attendeeCaptor.capture());
        assertThat(bookingCaptor.getValue().getUser()).isSameAs(host);
        assertThat(bookingCaptor.getValue().getTitle()).isEqualTo("Consultation with Jane");
        assertThat(bookingCaptor.getValue().getEndTime()).isEqualTo(end);
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        assertThat(attendeeCaptor.getValue().getBooking()).isSameAs(bookingCaptor.getValue());
        assertThat(attendeeCaptor.getValue().getPhone()).isEqualTo("+61400000000");
        assertThat(response.timezone()).isEqualTo("Australia/Sydney");
    }

    @Test
    void createBooking_whenAutoConfirmIsFalse_awaitsHost() {
        policy.setAutoConfirm(false);
        Instant start = Instant.parse("2026-09-23T23:30:00Z");
        Instant end = start.plusSeconds(1800);
        when(userRepository.findLockedByUserId(BigInteger.TWO)).thenReturn(Optional.of(host));
        when(slotService.getAvailableSlots(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24)
        )).thenReturn(new AvailableSlotsResponse(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24),
            "Australia/Sydney",
            List.of(new TimeSlot(start, end))
        ));
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(BookingEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.createBooking(served, "consultation", request(start));

        ArgumentCaptor<BookingEntity> captor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.AWAITING_HOST);
    }

    @Test
    void createBooking_whenSlotMissing_writesNothing() {
        Instant start = Instant.parse("2026-09-23T23:30:00Z");
        when(userRepository.findLockedByUserId(BigInteger.TWO)).thenReturn(Optional.of(host));
        when(slotService.getAvailableSlots(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24)
        )).thenReturn(new AvailableSlotsResponse(
            BigInteger.TEN,
            LocalDate.of(2026, 9, 24),
            "Australia/Sydney",
            List.of()
        ));

        assertThatThrownBy(() -> bookingService.createBooking(
            served,
            "consultation",
            request(start)
        )).isInstanceOf(SlotUnavailableException.class);
        verifyNoInteractions(bookingRepository, attendeeRepository);
    }

    private WidgetBookingRequest request(Instant start) {
        return new WidgetBookingRequest(
            "Jane",
            "+61400000000",
            "jane@example.com",
            "America/New_York",
            start,
            "Window seat"
        );
    }
}
