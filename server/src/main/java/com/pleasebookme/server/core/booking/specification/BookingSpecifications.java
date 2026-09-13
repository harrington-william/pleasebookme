package com.pleasebookme.server.core.booking.specification;

import com.pleasebookme.server.core.booking.entity.BookingEntity;
import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;
import com.pleasebookme.server.core.enums.BookingStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

public final class BookingSpecifications {
    private BookingSpecifications() {}

    public static Specification<BookingEntity> hasOrganization(BigInteger organizationId) {
        return (root, query, builder) -> builder.equal(
            root.get("service").get("organization").get("organizationId"),
            organizationId
        );
    }

    public static Specification<BookingEntity> hasService(BigInteger serviceId) {
        if (serviceId == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.equal(
            root.get("service").get("serviceId"),
            serviceId
        );
    }

    public static Specification<BookingEntity> hasResource(BigInteger resourceId) {
        if (resourceId == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> {
            Subquery<BigInteger> subquery = query.subquery(BigInteger.class);
            Root<BookingResourceEntity> bookingResource = subquery.from(BookingResourceEntity.class);

            subquery.select(bookingResource.get("booking").get("bookingId"))
                .where(
                    builder.equal(
                        bookingResource.get("booking").get("bookingId"),
                        root.get("bookingId")
                    ),
                    builder.equal(
                        bookingResource.get("resource").get("resourceId"),
                        resourceId
                    )
                );

            return builder.exists(subquery);
        };
    }

    public static Specification<BookingEntity> startsBetween(Instant from, Instant to) {
        if (from == null || to == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.and(
            builder.greaterThanOrEqualTo(root.get("startTime"), from),
            builder.lessThan(root.get("startTime"), to)
        );
    }

    public static Specification<BookingEntity> hasStatusIn(Collection<BookingStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> root.get("status").in(statuses);
    }

    public static Specification<BookingEntity> matchesText(String text) {
        if (text == null || text.isBlank()) {
            return Specification.unrestricted();
        }

        String pattern = "%" + escapeLikeWildcards(text.trim().toLowerCase()) + "%";

        return (root, query, builder) ->
            builder.like(builder.lower(root.get("title")), pattern, '\\');
    }

    public static Specification<BookingEntity> matchesTab(BookingTab tab, Instant now) {
        return switch (tab) {
            case UPCOMING -> (root, query, builder) -> builder.and(
                builder.greaterThanOrEqualTo(root.get("endTime"), now),
                root.get("status").in(
                    BookingStatus.PENDING,
                    BookingStatus.ACCEPTED,
                    BookingStatus.AWAITING_HOST
                )
            );
            case PENDING -> (root, query, builder) ->
                root.get("status").in(BookingStatus.PENDING, BookingStatus.AWAITING_HOST);
            case PAST -> (root, query, builder) -> builder.and(
                builder.lessThan(root.get("endTime"), now),
                root.get("status").in(
                    BookingStatus.PENDING,
                    BookingStatus.ACCEPTED,
                    BookingStatus.AWAITING_HOST
                )
            );
            case CANCELLED -> (root, query, builder) ->
                root.get("status").in(BookingStatus.CANCELLED, BookingStatus.REJECTED);
            case ALL -> Specification.unrestricted();
        };
    }

    private static String escapeLikeWildcards(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}
