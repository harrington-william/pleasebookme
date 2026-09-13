package com.pleasebookme.server.core.service;

import com.pleasebookme.server.core.service.dto.ServiceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ServiceRequestJsonTest {
    @Autowired private ObjectMapper objectMapper;

    @Test
    void serviceRequestBinding_ignoresLegacyOwnershipFields() throws Exception {
        ServiceRequest request = objectMapper.readValue("""
            {
              "title": "Consultation",
              "slug": "consultation",
              "description": "Planning session",
              "userId": 999,
              "profileId": 999,
              "organizationId": 999,
              "scheduleId": 11
            }
            """, ServiceRequest.class);

        assertThat(request.title()).isEqualTo("Consultation");
        assertThat(request.scheduleId()).isEqualTo(BigInteger.valueOf(11));
        assertThat(ServiceRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("userId", "profileId", "organizationId");
    }
}
