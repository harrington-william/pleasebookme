package com.pleasebookme.server.service.availabilityruleset;

import com.pleasebookme.server.auth.enums.AccountStatus;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.core.availability.entity.AvailabilityEntity;
import com.pleasebookme.server.core.availability.repository.AvailabilityRepository;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.security.identity.context.CurrentPrincipalProvider;
import com.pleasebookme.server.security.identity.enums.AuthenticatedActorType;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetRequest;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityRulesetResponse;
import com.pleasebookme.server.service.availabilityruleset.dto.AvailabilityWindowRequest;
import com.pleasebookme.server.service.availabilityruleset.service.impl.AvailabilityRulesetServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityRulesetServiceImplTest {
    private static final BigInteger USER_ID = BigInteger.valueOf(42);
    private static final BigInteger SCHEDULE_ID = BigInteger.valueOf(7);

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentPrincipalProvider currentPrincipalProvider;

    private AvailabilityRulesetServiceImpl service;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        service = new AvailabilityRulesetServiceImpl(
            scheduleRepository,
            availabilityRepository,
            userRepository,
            currentPrincipalProvider
        );

        user = UserEntity.builder().username("jane").build();
        user.setUserId(USER_ID);
    }

    @Test
    void createAvailabilityRuleset_createsScheduleAndWindowsForCurrentPrincipal() {
        AtomicReference<List<AvailabilityEntity>> capturedAvailabilities = new AtomicReference<>();

        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> {
            ScheduleEntity schedule = invocation.getArgument(0);
            schedule.setScheduleId(SCHEDULE_ID);
            return schedule;
        });
        when(availabilityRepository.saveAll(anyIterable())).thenAnswer(invocation -> {
            List<AvailabilityEntity> availabilities = invocation.getArgument(0);
            capturedAvailabilities.set(availabilities);
            for (int index = 0; index < availabilities.size(); index++) {
                availabilities.get(index).setAvailabilityId(BigInteger.valueOf(index + 1L));
            }
            return availabilities;
        });

        AvailabilityRulesetResponse response = service.createAvailabilityRuleset(request());

        assertThat(response.schedule().userId()).isEqualTo(USER_ID);
        assertThat(response.schedule().scheduleId()).isEqualTo(SCHEDULE_ID);
        assertThat(response.availabilities()).hasSize(2);
        assertThat(response.availabilities())
            .extracting(availability -> availability.userId())
            .containsOnly(USER_ID);
        assertThat(response.availabilities())
            .extracting(availability -> availability.scheduleId())
            .containsOnly(SCHEDULE_ID);

        ArgumentCaptor<ScheduleEntity> scheduleCaptor =
            ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository).save(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getUser()).isSameAs(user);

        assertThat(capturedAvailabilities.get())
            .hasSize(2)
            .allSatisfy(availability -> {
                assertThat(availability.getUser()).isSameAs(user);
                assertThat(availability.getSchedule()).isSameAs(scheduleCaptor.getValue());
            });
    }

    @Test
    void createAvailabilityRuleset_resolvedPrincipalUserMustExist() {
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAvailabilityRuleset(request()))
            .isInstanceOf(UserNotFoundException.class);

        verify(scheduleRepository, never()).save(any());
        verify(availabilityRepository, never()).saveAll(anyIterable());
    }

    @Test
    void createAvailabilityRuleset_windowFailureReliesOnTransactionalBoundaryForRollback() {
        when(currentPrincipalProvider.requireUser()).thenReturn(principal(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(availabilityRepository.saveAll(anyIterable()))
            .thenThrow(new RuntimeException("flush failed"));

        assertThatThrownBy(() -> service.createAvailabilityRuleset(request()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("flush failed");

        InOrder order = inOrder(scheduleRepository, availabilityRepository);
        order.verify(scheduleRepository).save(any());
        order.verify(availabilityRepository).saveAll(anyIterable());
    }

    @Test
    void createAvailabilityRuleset_transactionBoundaryLivesOnPublicServiceMethod() throws Exception {
        Method method = AvailabilityRulesetServiceImpl.class.getMethod(
            "createAvailabilityRuleset",
            AvailabilityRulesetRequest.class
        );

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void createAvailabilityRuleset_requestTypesHaveNoUserIdAccessor() {
        assertThat(AvailabilityRulesetRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("userId");
        assertThat(AvailabilityWindowRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("userId");
    }

    private static AvailabilityRulesetRequest request() {
        return new AvailabilityRulesetRequest(
            "Working Hours",
            "Asia/Ho_Chi_Minh",
            List.of(
                new AvailabilityWindowRequest(
                    List.of(1, 2, 3),
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0)
                ),
                new AvailabilityWindowRequest(
                    List.of(4, 5),
                    LocalTime.of(13, 0),
                    LocalTime.of(17, 0)
                )
            )
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
