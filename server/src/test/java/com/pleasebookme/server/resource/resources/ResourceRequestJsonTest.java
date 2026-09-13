package com.pleasebookme.server.resource.resources;

import com.pleasebookme.server.resource.enums.ResourceStatus;
import com.pleasebookme.server.resource.resources.dto.ResourceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidFormatException;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
class ResourceRequestJsonTest {
    @Autowired private ObjectMapper objectMapper;

    @Test
    void resourceRequestBinding_acceptsOptionalDescriptionAndCapacity() throws Exception {
        ResourceRequest request = objectMapper.readValue("""
            {
              "resourceTypeId": 11,
              "name": "Conference Room A",
              "slug": "conference-room-a",
              "status": "ACTIVE"
            }
            """, ResourceRequest.class);

        assertThat(request.resourceTypeId()).isEqualTo(BigInteger.valueOf(11));
        assertThat(request.description()).isNull();
        assertThat(request.capacity()).isNull();
        assertThat(request.status()).isEqualTo(ResourceStatus.ACTIVE);
        assertThat(ResourceRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("serviceId");
    }

    @Test
    void resourceRequestBinding_rejectsUnknownStatus() {
        assertThatThrownBy(() -> objectMapper.readValue("""
            {
              "resourceTypeId": 11,
              "name": "Conference Room A",
              "slug": "conference-room-a",
              "status": "BANANA"
            }
            """, ResourceRequest.class))
            .isInstanceOf(InvalidFormatException.class);
    }
}
