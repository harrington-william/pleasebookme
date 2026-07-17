package com.pleasebookme.server.auth.user.dto;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.global.enums.Theme;
import com.pleasebookme.server.global.enums.WeekStart;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    BigInteger userId,
    UUID userUid,
    String username,
    String name,
    String email,
    String phone,
    String bio,
    String avatarUrl,
    Locale locale,
    String timezone,
    Theme theme,
    WeekStart weekStart,
    AccountStatus accountStatus,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
            user.getUserId(),
            user.getUserUid(),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getLocale(),
            user.getTimezone(),
            user.getTheme(),
            user.getWeekStart(),
            user.getAccountStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
