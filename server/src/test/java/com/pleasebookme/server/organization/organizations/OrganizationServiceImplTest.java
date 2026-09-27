package com.pleasebookme.server.organization.organizations;

import com.pleasebookme.server.organization.organizations.dto.OrganizationRequest;
import com.pleasebookme.server.organization.organizations.exception.DuplicateOrganizationException;
import com.pleasebookme.server.organization.organizations.repository.OrganizationRepository;
import com.pleasebookme.server.organization.organizations.service.impl.OrganizationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {
    @Mock private OrganizationRepository organizationRepository;

    @Test
    void createOrganization_rejectsReservedSlugCaseInsensitively() {
        OrganizationServiceImpl service = new OrganizationServiceImpl(organizationRepository);

        assertThatThrownBy(() -> service.createOrganization(request("LOGIN")))
            .isInstanceOf(DuplicateOrganizationException.class)
            .hasMessage("Slug is reserved: LOGIN");
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void updateOrganization_rejectsReservedSlugBeforeLookup() {
        OrganizationServiceImpl service = new OrganizationServiceImpl(organizationRepository);

        assertThatThrownBy(() -> service.updateOrganization(
            BigInteger.ONE,
            request("login")
        )).isInstanceOf(DuplicateOrganizationException.class)
            .hasMessage("Slug is reserved: login");
        verifyNoInteractions(organizationRepository);
    }

    private OrganizationRequest request(String slug) {
        return new OrganizationRequest(
            "Acme",
            slug,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
