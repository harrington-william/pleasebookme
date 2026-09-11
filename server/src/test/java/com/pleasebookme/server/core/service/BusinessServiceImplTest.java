package com.pleasebookme.server.core.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.core.bookingpolicy.dto.ServiceBookingPolicyRequest;
import com.pleasebookme.server.core.bookingpolicy.entity.BookingPolicyEntity;
import com.pleasebookme.server.core.bookingpolicy.repository.BookingPolicyRepository;
import com.pleasebookme.server.core.enums.BookingMode;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.core.schedule.repository.ScheduleRepository;
import com.pleasebookme.server.core.service.dto.ServiceRequest;
import com.pleasebookme.server.core.service.entity.ServiceEntity;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.core.service.repository.ServiceRepository;
import com.pleasebookme.server.core.service.dto.ServiceCreateResult;
import com.pleasebookme.server.core.service.service.impl.BusinessServiceImpl;
import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.integration.calendar.repository.DestinationCalendarRepository;
import com.pleasebookme.server.integration.sheets.repository.DestinationSheetsRepository;
import com.pleasebookme.server.organization.membership.entity.MembershipEntity;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import com.pleasebookme.server.service.organization.context.OrganizationContext;
import com.pleasebookme.server.service.organization.exception.AmbiguousOrganizationContextException;
import com.pleasebookme.server.service.organization.service.CurrentOrganizationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceImplTest {
    private static final BigInteger USER_ID = BigInteger.valueOf(42);
    private static final BigInteger PROFILE_ID = BigInteger.valueOf(24);
    private static final BigInteger ORGANIZATION_ID = BigInteger.valueOf(7);
    private static final BigInteger MEMBERSHIP_ID = BigInteger.valueOf(70);
    private static final BigInteger SCHEDULE_ID = BigInteger.valueOf(11);
    private static final BigInteger SERVICE_ID = BigInteger.valueOf(15);

    @Mock private ServiceRepository serviceRepository;
    @Mock private BookingPolicyRepository bookingPolicyRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private DestinationCalendarRepository destinationCalendarRepository;
    @Mock private DestinationSheetsRepository destinationSheetsRepository;
    @Mock private CurrentOrganizationProvider currentOrganizationProvider;

    private BusinessServiceImpl service;
    private UserEntity user;
    private ProfileEntity profile;
    private OrganizationEntity organization;
    private MembershipEntity membership;
    private ScheduleEntity schedule;
    private OrganizationContext organizationContext;

    @BeforeEach
    void setUp() {
        service = new BusinessServiceImpl(
            serviceRepository,
            bookingPolicyRepository,
            scheduleRepository,
            destinationCalendarRepository,
            destinationSheetsRepository,
            currentOrganizationProvider
        );

        user = UserEntity.builder().username("jane").build();
        user.setUserId(USER_ID);

        organization = OrganizationEntity.builder().name("Studio").slug("studio").build();
        organization.setOrganizationId(ORGANIZATION_ID);

        membership = MembershipEntity.builder()
            .user(user)
            .organization(organization)
            .accepted(true)
            .build();
        membership.setMembershipId(MEMBERSHIP_ID);

        profile = ProfileEntity.builder()
            .user(user)
            .organization(organization)
            .username("jane")
            .build();
        profile.setProfileId(PROFILE_ID);

        schedule = ScheduleEntity.builder().title("Working Hours").user(user).build();
        schedule.setScheduleId(SCHEDULE_ID);

        organizationContext = new OrganizationContext(user, membership, organization);
    }

    @Test
    void createService_derivesOwnerAndCreatesNestedBookingPolicy() {
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(currentOrganizationProvider.requireProfile(organizationContext)).thenReturn(profile);
        when(serviceRepository.existsByOrganizationOrganizationIdAndSlug(ORGANIZATION_ID, "consultation"))
            .thenReturn(false);
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(java.util.Optional.of(schedule));
        when(serviceRepository.save(any())).thenAnswer(invocation -> {
            ServiceEntity saved = invocation.getArgument(0);
            saved.setServiceId(SERVICE_ID);
            return saved;
        });
        when(bookingPolicyRepository.save(any())).thenAnswer(invocation -> {
            BookingPolicyEntity saved = invocation.getArgument(0);
            saved.setBookingPolicyId(BigInteger.valueOf(99));
            return saved;
        });

        ServiceCreateResult result = service.createService(request(bookingPolicyRequest()));

        assertThat(result.service().getUser()).isSameAs(user);
        assertThat(result.service().getProfile()).isSameAs(profile);
        assertThat(result.service().getOrganization()).isSameAs(organization);
        assertThat(result.bookingPolicy()).isNotNull();
        assertThat(result.bookingPolicy().getService()).isSameAs(result.service());
        assertThat(result.bookingPolicy().getBookingMode()).isEqualTo(BookingMode.FIXED);
        assertThat(result.bookingPolicy().getDefaultDuration()).isEqualTo(60);
        assertThat(result.bookingPolicy().getMinimumNotice()).isEqualTo(30);

        verify(bookingPolicyRepository, never()).existsByServiceServiceId(any());
    }

    @Test
    void createService_rejectsCallerWithNoOrganizationContext() {
        when(currentOrganizationProvider.requireCurrent())
            .thenThrow(new AmbiguousOrganizationContextException("ambiguous"));

        assertThatThrownBy(() -> service.createService(request(null)))
            .isInstanceOf(AmbiguousOrganizationContextException.class);

        verify(scheduleRepository, never()).findById(any());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void createService_policyFailureReliesOnTransactionalBoundaryForRollback() {
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(currentOrganizationProvider.requireProfile(organizationContext)).thenReturn(profile);
        when(serviceRepository.existsByOrganizationOrganizationIdAndSlug(ORGANIZATION_ID, "consultation"))
            .thenReturn(false);
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(java.util.Optional.of(schedule));
        when(serviceRepository.save(any())).thenAnswer(invocation -> {
            ServiceEntity saved = invocation.getArgument(0);
            saved.setServiceId(SERVICE_ID);
            return saved;
        });
        when(bookingPolicyRepository.save(any())).thenThrow(new RuntimeException("flush failed"));

        assertThatThrownBy(() -> service.createService(request(bookingPolicyRequest())))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("flush failed");

        InOrder order = inOrder(serviceRepository, bookingPolicyRepository);
        order.verify(serviceRepository).save(any());
        order.verify(bookingPolicyRepository).save(any());
    }

    @Test
    void updateService_succeedsWhenCallerBelongsToOwningOrganization() {
        ServiceEntity existing = ServiceEntity.builder()
            .title("Old")
            .slug("old")
            .user(user)
            .profile(profile)
            .organization(organization)
            .schedule(schedule)
            .build();
        existing.setServiceId(SERVICE_ID);

        when(serviceRepository.findById(SERVICE_ID)).thenReturn(java.util.Optional.of(existing));
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(java.util.Optional.of(schedule));
        when(serviceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceEntity updated = service.updateService(SERVICE_ID, request(null)).service();

        assertThat(updated.getTitle()).isEqualTo("Consultation");
        assertThat(updated.getOrganization()).isSameAs(organization);
        assertThat(updated.getSchedule()).isSameAs(schedule);
    }

    @Test
    void updateService_rejectsCallerFromDifferentOrganization() {
        UserEntity otherUser = UserEntity.builder().username("other").build();
        otherUser.setUserId(BigInteger.valueOf(100));
        OrganizationEntity otherOrganization = OrganizationEntity.builder().name("Other").slug("other").build();
        otherOrganization.setOrganizationId(BigInteger.valueOf(101));
        ProfileEntity otherProfile = ProfileEntity.builder()
            .user(otherUser)
            .organization(otherOrganization)
            .username("other")
            .build();
        otherProfile.setProfileId(BigInteger.valueOf(102));

        ServiceEntity existing = ServiceEntity.builder()
            .title("Old")
            .slug("old")
            .user(otherUser)
            .profile(otherProfile)
            .organization(otherOrganization)
            .schedule(schedule)
            .build();
        existing.setServiceId(SERVICE_ID);

        when(serviceRepository.findById(SERVICE_ID)).thenReturn(java.util.Optional.of(existing));
        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);

        assertThatThrownBy(() -> service.updateService(SERVICE_ID, request(null)))
            .isInstanceOf(ServiceNotFoundException.class);

        verify(serviceRepository, never()).save(any());
        verify(scheduleRepository, never()).findById(any());
    }

    @Test
    void getServiceById_includesBookingPolicyWhenOneExists() {
        ServiceEntity existing = ServiceEntity.builder()
            .title("Old")
            .slug("old")
            .user(user)
            .profile(profile)
            .organization(organization)
            .schedule(schedule)
            .build();
        existing.setServiceId(SERVICE_ID);

        BookingPolicyEntity policy = BookingPolicyEntity.builder().service(existing).build();
        policy.setBookingPolicyId(BigInteger.valueOf(99));

        when(serviceRepository.findById(SERVICE_ID)).thenReturn(java.util.Optional.of(existing));
        when(bookingPolicyRepository.findByServiceServiceId(SERVICE_ID)).thenReturn(java.util.Optional.of(policy));

        ServiceCreateResult result = service.getServiceById(SERVICE_ID);

        assertThat(result.service()).isSameAs(existing);
        assertThat(result.bookingPolicy()).isSameAs(policy);
    }

    @Test
    void getServiceById_bookingPolicyNullWhenNoneExists() {
        ServiceEntity existing = ServiceEntity.builder()
            .title("Old")
            .slug("old")
            .user(user)
            .profile(profile)
            .organization(organization)
            .schedule(schedule)
            .build();
        existing.setServiceId(SERVICE_ID);

        when(serviceRepository.findById(SERVICE_ID)).thenReturn(java.util.Optional.of(existing));
        when(bookingPolicyRepository.findByServiceServiceId(SERVICE_ID)).thenReturn(java.util.Optional.empty());

        ServiceCreateResult result = service.getServiceById(SERVICE_ID);

        assertThat(result.bookingPolicy()).isNull();
    }

    @Test
    void getServicesByOrganizationId_returnsServicesForCallerOrganization() {
        ServiceEntity existing = ServiceEntity.builder()
            .title("Consultation")
            .slug("consultation")
            .user(user)
            .profile(profile)
            .organization(organization)
            .schedule(schedule)
            .build();
        existing.setServiceId(SERVICE_ID);

        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);
        when(serviceRepository.findByOrganizationOrganizationId(ORGANIZATION_ID)).thenReturn(List.of(existing));

        List<ServiceEntity> result = service.getServicesByOrganizationId(ORGANIZATION_ID);

        assertThat(result).containsExactly(existing);
    }

    @Test
    void getServicesByOrganizationId_returnsEmptyForForeignOrganization() {
        BigInteger foreignOrganizationId = BigInteger.valueOf(900);

        when(currentOrganizationProvider.requireCurrent()).thenReturn(organizationContext);

        List<ServiceEntity> result = service.getServicesByOrganizationId(foreignOrganizationId);

        assertThat(result).isEmpty();
        verify(serviceRepository, never()).findByOrganizationOrganizationId(any());
    }

    @Test
    void getServicesByOrganizationId_rejectsCallerWithNoOrganizationContext() {
        when(currentOrganizationProvider.requireCurrent())
            .thenThrow(new AmbiguousOrganizationContextException("ambiguous"));

        assertThatThrownBy(() -> service.getServicesByOrganizationId(ORGANIZATION_ID))
            .isInstanceOf(AmbiguousOrganizationContextException.class);

        verify(serviceRepository, never()).findByOrganizationOrganizationId(any());
    }

    @Test
    void createService_transactionBoundaryLivesOnPublicServiceMethod() throws Exception {
        Method method = BusinessServiceImpl.class.getMethod(
            "createService",
            ServiceRequest.class
        );

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void updateService_transactionBoundaryLivesOnPublicServiceMethod() throws Exception {
        Method method = BusinessServiceImpl.class.getMethod(
            "updateService",
            BigInteger.class,
            ServiceRequest.class
        );

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void serviceRequestTypeHasNoOwnershipAccessors() {
        assertThat(ServiceRequest.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("userId", "profileId", "organizationId")
            .contains("bookingPolicy");
    }

    private static ServiceRequest request(ServiceBookingPolicyRequest bookingPolicy) {
        return new ServiceRequest(
            "Consultation",
            "consultation",
            "Planning session",
            Locale.en,
            "Online",
            SCHEDULE_ID,
            "UNLIMITED",
            "Asia/Ho_Chi_Minh",
            BigDecimal.valueOf(25),
            BigDecimal.valueOf(25),
            Currency.USD,
            false,
            false,
            false,
            "https://example.com/thanks",
            false,
            1,
            null,
            null,
            bookingPolicy
        );
    }

    private static ServiceBookingPolicyRequest bookingPolicyRequest() {
        return new ServiceBookingPolicyRequest(
            BookingMode.FIXED,
            60,
            null,
            null,
            30,
            90,
            30,
            0,
            0,
            false,
            false,
            false,
            true,
            "ROLLING",
            1
        );
    }
}
