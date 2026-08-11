package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.global.enums.Locale;

public interface UserProvisioningService {
    UserEntity provisionUser(
        String username,
        String name,
        String email,
        String phone,
        Locale locale,
        String timezone
    );
}
