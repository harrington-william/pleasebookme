package com.pleasebookme.server.core.schedule;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.schedule.dto.ScheduleRequest;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.schedule.service.impl.ScheduleServiceImpl;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {
    private static final BigInteger USER_ID = BigInteger.valueOf(42);
    private static final BigInteger SCHEDULE_ID = BigInteger.valueOf(7);

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentPrincipalProvider currentPrincipalProvider;

    private ScheduleServiceImpl service;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        service = new ScheduleServiceImpl(
            scheduleRepository,
            userRepository,
            currentPrincipalProvider
        );

        user = UserEntity.builder().username("jane").build();
        user.setUserId(USER_ID);
    }

    @Test
    void createSchedule_derivesOwnerFromCurrentPrincipal() {
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleEntity schedule = service.createSchedule(
            new ScheduleRequest("Working Hours", "Asia/Ho_Chi_Minh")
        );

        assertThat(schedule.getUser()).isSameAs(user);
        assertThat(schedule.getTitle()).isEqualTo("Working Hours");
        assertThat(schedule.getTimezone()).isEqualTo("Asia/Ho_Chi_Minh");
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void updateSchedule_requiresCurrentPrincipalButKeepsExistingOwner() {
        UserEntity originalOwner = UserEntity.builder().username("owner").build();
        originalOwner.setUserId(BigInteger.valueOf(100));

        ScheduleEntity existing = ScheduleEntity.builder()
            .user(originalOwner)
            .title("Old")
            .timezone("UTC")
            .build();

        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(existing));
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleEntity schedule = service.updateSchedule(
            SCHEDULE_ID,
            new ScheduleRequest("Updated", "Asia/Ho_Chi_Minh")
        );

        assertThat(schedule.getUser()).isSameAs(originalOwner);
        assertThat(schedule.getTitle()).isEqualTo("Updated");
        assertThat(schedule.getTimezone()).isEqualTo("Asia/Ho_Chi_Minh");
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void createSchedule_resolvedPrincipalUserMustExist() {
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSchedule(
            new ScheduleRequest("Working Hours", null)
        )).isInstanceOf(UserNotFoundException.class);

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void createSchedule_requestTypeHasNoUserIdAccessor() {
        assertThat(ScheduleRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("userId");
    }

    private static UserPrincipal principal(BigInteger userId) {
        return new UserPrincipal(
            AuthenticatedActorType.USER,
            UUID.randomUUID(),
            userId,
            null,
            "jane",
            "jane@example.com",
            "Jane Doe",
            null,
            "Asia/Ho_Chi_Minh",
            AccountStatus.ACTIVE,
            Set.of(),
            Set.of(),
            Map.of()
        );
    }
}
