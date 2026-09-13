package com.pleasebookme.server.core.bookingpolicy;

import com.pleasebookme.server.core.bookingpolicy.dto.BookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.exception.DuplicateBookingPolicyException;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.bookingpolicy.service.impl.BookingPolicyServiceImpl;
import com.pleasebookme.server.core.enums.BookingMode;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingPolicyServiceImplTest {
    private static final BigInteger SERVICE_ID = BigInteger.valueOf(15);

    @Mock private BookingPolicyRepository bookingPolicyRepository;
    @Mock private ServiceRepository serviceRepository;

    private BookingPolicyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BookingPolicyServiceImpl(bookingPolicyRepository, serviceRepository);
    }

    @Test
    void createBookingPolicy_rejectsDuplicateServicePolicy() {
        when(bookingPolicyRepository.existsByServiceServiceId(SERVICE_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createBookingPolicy(request()))
            .isInstanceOf(DuplicateBookingPolicyException.class);

        verify(serviceRepository, never()).findById(any());
        verify(bookingPolicyRepository, never()).save(any());
    }

    @Test
    void createBookingPolicy_savesWhenServiceHasNoPolicy() {
        ServiceEntity serviceEntity = ServiceEntity.builder().title("Consultation").build();
        serviceEntity.setServiceId(SERVICE_ID);

        when(bookingPolicyRepository.existsByServiceServiceId(SERVICE_ID)).thenReturn(false);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(serviceEntity));
        when(bookingPolicyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingPolicyEntity bookingPolicy = service.createBookingPolicy(request());

        assertThat(bookingPolicy.getService()).isSameAs(serviceEntity);
        assertThat(bookingPolicy.getBookingMode()).isEqualTo(BookingMode.FIXED);
        assertThat(bookingPolicy.getDefaultDuration()).isEqualTo(60);
        assertThat(bookingPolicy.getMinimumNotice()).isEqualTo(30);
    }

    @Test
    void getBookingPolicyByServiceId_returnsMatchingPolicy() {
        ServiceEntity serviceEntity = ServiceEntity.builder().title("Consultation").build();
        serviceEntity.setServiceId(SERVICE_ID);
        BookingPolicyEntity policy = BookingPolicyEntity.builder()
            .service(serviceEntity)
            .bookingMode(BookingMode.FIXED)
            .defaultDuration(60)
            .build();
        policy.setBookingPolicyId(BigInteger.valueOf(99));

        when(bookingPolicyRepository.findByServiceServiceId(SERVICE_ID)).thenReturn(Optional.of(policy));

        Optional<BookingPolicyEntity> result = service.getBookingPolicyByServiceId(SERVICE_ID);

        assertThat(result).containsSame(policy);
    }

    @Test
    void getBookingPolicyByServiceId_returnsEmptyWhenServiceHasNoPolicy() {
        when(bookingPolicyRepository.findByServiceServiceId(SERVICE_ID)).thenReturn(Optional.empty());

        Optional<BookingPolicyEntity> result = service.getBookingPolicyByServiceId(SERVICE_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllBookingPolicies_remainsUnfiltered() {
        BookingPolicyEntity first = BookingPolicyEntity.builder().build();
        BookingPolicyEntity second = BookingPolicyEntity.builder().build();
        when(bookingPolicyRepository.findAll()).thenReturn(List.of(first, second));

        List<BookingPolicyEntity> result = service.getAllBookingPolicies();

        assertThat(result).containsExactly(first, second);
        verify(bookingPolicyRepository, never()).findByServiceServiceId(any());
    }

    private static BookingPolicyRequest request() {
        return new BookingPolicyRequest(
            SERVICE_ID,
            BookingMode.FIXED,
            60,
            null,
            null,
            30,
            90,
            30,
            0,
            0,
            false,
            false,
            false,
            true,
            "ROLLING",
            1
        );
    }
}
