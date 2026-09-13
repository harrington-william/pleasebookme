package com.pleasebookme.server.core.booking;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.booking.specification.BookingSpecifications;
import com.pleasebookme.server.core.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;
import java.time.Instant;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class BookingSpecificationsTest {
    private static final BigInteger HOST_USER_ID = BigInteger.valueOf(1);
    private static final Instant FROM = Instant.parse("2026-09-20T13:45:00Z");
    private static final Instant TO = Instant.parse("2026-09-21T14:15:00Z");

    @Test
    void slotConflictComposition_composes() {
        Specification<BookingEntity> specification = Specification.allOf(
            BookingSpecifications.hasServiceOwnedBy(HOST_USER_ID),
            BookingSpecifications.hasStatusIn(
                EnumSet.of(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.AWAITING_HOST)
            ),
            BookingSpecifications.overlaps(FROM, TO),
            BookingSpecifications.isNotDeleted()
        );

        assertThat(specification).isNotNull();
    }

    /**
     * Same reason as {@code ResourceSpecificationsTest}: Spring Data JPA 4.x asserts
     * non-null inside {@code Specification.and}, so a factory that returned
     * {@code null} for an absent bound would fail the whole composition.
     */
    @Test
    void overlaps_composesWhenEitherBoundIsAbsent() {
        assertThatCode(() -> Specification.allOf(
            BookingSpecifications.hasServiceOwnedBy(HOST_USER_ID),
            BookingSpecifications.overlaps(null, TO),
            BookingSpecifications.isNotDeleted()
        )).doesNotThrowAnyException();

        assertThatCode(() -> Specification.allOf(
            BookingSpecifications.hasServiceOwnedBy(HOST_USER_ID),
            BookingSpecifications.overlaps(FROM, null),
            BookingSpecifications.isNotDeleted()
        )).doesNotThrowAnyException();
    }

    @Test
    void newFactories_neverReturnNull() {
        assertThat(BookingSpecifications.hasServiceOwnedBy(HOST_USER_ID)).isNotNull();
        assertThat(BookingSpecifications.overlaps(FROM, TO)).isNotNull();
        assertThat(BookingSpecifications.overlaps(null, null)).isNotNull();
        assertThat(BookingSpecifications.isNotDeleted()).isNotNull();
    }
}
