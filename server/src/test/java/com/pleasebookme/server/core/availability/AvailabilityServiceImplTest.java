package com.pleasebookme.server.core.availability;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.availability.dto.AvailabilityRequest;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.availability.service.impl.AvailabilityServiceImpl;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalTime;
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
class AvailabilityServiceImplTest {
    private static final BigInteger USER_ID = BigInteger.valueOf(42);
    private static final BigInteger SCHEDULE_ID = BigInteger.valueOf(7);
    private static final BigInteger AVAILABILITY_ID = BigInteger.valueOf(11);
    private static final Integer[] DAYS = new Integer[] {1, 2, 3};

    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private CurrentPrincipalProvider currentPrincipalProvider;

    private AvailabilityServiceImpl service;
    private UserEntity user;
    private ScheduleEntity schedule;

    @BeforeEach
    void setUp() {
        service = new AvailabilityServiceImpl(
            availabilityRepository,
            userRepository,
            scheduleRepository,
            currentPrincipalProvider
        );

        user = UserEntity.builder().username("jane").build();
        user.setUserId(USER_ID);

        schedule = ScheduleEntity.builder().title("Working Hours").user(user).build();
        schedule.setScheduleId(SCHEDULE_ID);
    }

    @Test
    void createAvailability_derivesOwnerFromCurrentPrincipal() {
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
        when(availabilityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AvailabilityEntity availability = service.createAvailability(request());

        assertThat(availability.getUser()).isSameAs(user);
        assertThat(availability.getSchedule()).isSameAs(schedule);
        assertThat(availability.getDays()).containsExactly(DAYS);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void updateAvailability_requiresCurrentPrincipalButKeepsExistingOwner() {
        UserEntity originalOwner = UserEntity.builder().username("owner").build();
        originalOwner.setUserId(BigInteger.valueOf(100));

        AvailabilityEntity existing = AvailabilityEntity.builder()
            .user(originalOwner)
            .schedule(ScheduleEntity.builder().title("Old").build())
            .days(new Integer[] {5})
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(9, 0))
            .build();

        when(availabilityRepository.findById(AVAILABILITY_ID)).thenReturn(Optional.of(existing));
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
        when(availabilityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AvailabilityEntity availability = service.updateAvailability(AVAILABILITY_ID, request());

        assertThat(availability.getUser()).isSameAs(originalOwner);
        assertThat(availability.getSchedule()).isSameAs(schedule);
        assertThat(availability.getDays()).containsExactly(DAYS);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void createAvailability_resolvedPrincipalUserMustExist() {
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAvailability(request()))
            .isInstanceOf(UserNotFoundException.class);

        verify(scheduleRepository, never()).findById(any());
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_requestTypeHasNoUserIdAccessor() {
        assertThat(AvailabilityRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("userId");
    }

    private static AvailabilityRequest request() {
        return new AvailabilityRequest(
            SCHEDULE_ID,
            DAYS,
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        );
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
