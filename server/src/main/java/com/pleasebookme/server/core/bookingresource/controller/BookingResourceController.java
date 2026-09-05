package com.pleasebookme.server.core.bookingresource.controller;

import com.pleasebookme.server.core.bookingresource.dto.BookingResourceRequest;
import com.pleasebookme.server.core.bookingresource.dto.BookingResourceResponse;
import com.pleasebookme.server.core.bookingresource.entity.BookingResourceEntity;
import com.pleasebookme.server.core.bookingresource.service.BookingResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking-resources")
@RequiredArgsConstructor
public class BookingResourceController {
    private final BookingResourceService bookingResourceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResourceResponse createBookingResource(
        @Valid @RequestBody BookingResourceRequest request
    ) {
        return BookingResourceResponse.from(
            bookingResourceService.createBookingResource(request)
        );
    }

    @GetMapping("/{bookingId}/{resourceId}")
    public BookingResourceResponse getBookingResource(
        @PathVariable BigInteger bookingId,
        @PathVariable BigInteger resourceId
    ) {
        return BookingResourceResponse.from(
            bookingResourceService.getBookingResourceById(bookingId, resourceId)
        );
    }

    @GetMapping
    public List<BookingResourceResponse> getBookingResources(
        @RequestParam(required = false) BigInteger bookingId,
        @RequestParam(required = false) BigInteger resourceId,
        @RequestParam(required = false) BigInteger organizationId
    ) {
        List<BookingResourceEntity> bookingResources;

        if (bookingId != null) {
            bookingResources = bookingResourceService.getBookingResourcesByBookingId(bookingId);
        } else if (resourceId != null) {
            bookingResources = bookingResourceService.getBookingResourcesByResourceId(resourceId);
        } else if (organizationId != null) {
            bookingResources = bookingResourceService.getBookingResourcesByOrganizationId(organizationId);
        } else {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "bookingId, resourceId, or organizationId is required."
            );
        }

        return bookingResources.stream()
            .map(BookingResourceResponse::from)
            .toList();
    }

    @DeleteMapping("/{bookingId}/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookingResource(
        @PathVariable BigInteger bookingId,
        @PathVariable BigInteger resourceId
    ) {
        bookingResourceService.deleteBookingResource(bookingId, resourceId);
    }
}
