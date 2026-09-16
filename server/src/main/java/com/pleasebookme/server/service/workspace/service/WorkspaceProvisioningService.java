package com.pleasebookme.server.service.workspace.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.service.workspace.dto.WorkspaceProvisionRequest;

public interface WorkspaceProvisioningService {
    UserEntity provision(WorkspaceProvisionRequest request);
}
