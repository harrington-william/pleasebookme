package com.pleasebookme.server.core.service.dto;

import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.service.entity.ServiceEntity;

public record ServiceCreateResult(
    ServiceEntity service,
    BookingPolicyEntity bookingPolicy
) {
}
