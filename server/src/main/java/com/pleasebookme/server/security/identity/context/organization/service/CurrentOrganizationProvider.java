package com.pleasebookme.server.security.identity.context.organization.service;

import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.security.identity.context.organization.context.OrganizationContext;

import java.math.BigInteger;

public interface CurrentOrganizationProvider {

    OrganizationContext requireCurrent();

    OrganizationContext require(BigInteger organizationId);

    ProfileEntity requireProfile(OrganizationContext context);
}
