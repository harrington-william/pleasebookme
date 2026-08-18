package com.pleasebookme.server.global.handler;

import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenExpiredException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenRevokedException;
import com.pleasebookme.server.security.token.jwt.exception.TokenExpiredException;
import com.pleasebookme.server.security.token.jwt.exception.WidgetOriginMismatchException;
import com.pleasebookme.server.widget.widgets.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.pleasebookme.server.audit.actor.exception.AuditActorNotFoundException;
import com.pleasebookme.server.audit.actor.exception.DuplicateAuditActorException;
import com.pleasebookme.server.audit.change.exception.AuditChangeNotFoundException;
import com.pleasebookme.server.audit.event.exception.AuditEventNotFoundException;
import com.pleasebookme.server.audit.resource.exception.AuditResourceNotFoundException;
import com.pleasebookme.server.auth.account.exception.AccountNotFoundException;
import com.pleasebookme.server.auth.account.exception.DuplicateAccountException;
import com.pleasebookme.server.auth.apikey.exception.ApiKeyNotFoundException;
import com.pleasebookme.server.auth.apikey.exception.DuplicateApiKeyException;
import com.pleasebookme.server.auth.password.exception.DuplicateUserPasswordException;
import com.pleasebookme.server.auth.password.exception.UserPasswordNotFoundException;
import com.pleasebookme.server.auth.permission.exception.DuplicatePermissionException;
import com.pleasebookme.server.auth.permission.exception.PermissionNotFoundException;
import com.pleasebookme.server.auth.refreshtoken.exception.DuplicateRefreshTokenException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenNotFoundException;
import com.pleasebookme.server.auth.role.exception.DuplicateRoleException;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.rolepermission.exception.DuplicateRolePermissionException;
import com.pleasebookme.server.auth.rolepermission.exception.RolePermissionNotFoundException;
import com.pleasebookme.server.auth.user.exception.DuplicateUserException;
import com.pleasebookme.server.auth.user.exception.UserEmailAlreadyExistException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.exception.UserPhoneNumberAlreadyExistException;
import com.pleasebookme.server.auth.user.exception.UsernameAlreadyExistException;
import com.pleasebookme.server.auth.userrole.exception.DuplicateUserRoleException;
import com.pleasebookme.server.auth.userrole.exception.UserRoleNotFoundException;
import com.pleasebookme.server.core.attendee.exception.AttendeeNotFoundException;
import com.pleasebookme.server.core.availability.exception.AvailabilityNotFoundException;
import com.pleasebookme.server.core.booking.exception.BookingNotFoundException;
import com.pleasebookme.server.core.bookingpolicy.exception.BookingPolicyNotFoundException;
import com.pleasebookme.server.core.outofoffice.exception.OutOfOfficeNotFoundException;
import com.pleasebookme.server.core.schedule.exception.ScheduleNotFoundException;
import com.pleasebookme.server.core.selectedslot.exception.DuplicateSelectedSlotException;
import com.pleasebookme.server.core.selectedslot.exception.SelectedSlotNotFoundException;
import com.pleasebookme.server.core.service.exception.DuplicateServiceException;
import com.pleasebookme.server.core.service.exception.ServiceNotFoundException;
import com.pleasebookme.server.customer.activity.exception.CustomerActivityNotFoundException;
import com.pleasebookme.server.customer.customers.exception.CustomerNotFoundException;
import com.pleasebookme.server.customer.customers.exception.DuplicateCustomerException;
import com.pleasebookme.server.customer.note.exception.CustomerNoteNotFoundException;
import com.pleasebookme.server.customer.source.exception.CustomerSourceNotFoundException;
import com.pleasebookme.server.customer.tag.exception.CustomerTagNotFoundException;
import com.pleasebookme.server.customer.tag.exception.DuplicateCustomerTagException;
import com.pleasebookme.server.global.exception.ResourceNotFoundException;
import com.pleasebookme.server.global.response.ApiErrorResponse;
import com.pleasebookme.server.integration.calendar.exception.DestinationCalendarNotFoundException;
import com.pleasebookme.server.integration.drive.exception.DestinationDriveNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.exception.DuplicateOAuthConnectionException;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.sheets.exception.DestinationSheetsNotFoundException;
import com.pleasebookme.server.integration.syncjob.exception.SyncJobNotFoundException;
import com.pleasebookme.server.security.oauth.google.exception.InvalidGoogleIdTokenException;
import com.pleasebookme.server.security.oauth.google.exception.GoogleTokenExchangeException;
import com.pleasebookme.server.security.oauth.google.exception.GoogleTokenRefreshException;
import com.pleasebookme.server.security.authorization.exception.AuthorizationDeniedException;
import com.pleasebookme.server.security.identity.exception.ForbiddenActorException;
import com.pleasebookme.server.security.identity.exception.UnauthenticatedException;
import com.pleasebookme.server.service.integration.exception.OAuthConnectionAccessDeniedException;
import com.pleasebookme.server.service.auth.exception.GoogleAccountEmailNotVerifiedException;
import com.pleasebookme.server.service.auth.exception.InvalidSessionHandoffException;
import com.pleasebookme.server.notification.channel.exception.DuplicateNotificationChannelException;
import com.pleasebookme.server.notification.channel.exception.NotificationChannelNotFoundException;
import com.pleasebookme.server.notification.delivery.exception.NotificationDeliveryNotFoundException;
import com.pleasebookme.server.notification.notifications.exception.NotificationNotFoundException;
import com.pleasebookme.server.notification.preferences.exception.DuplicateNotificationPreferenceException;
import com.pleasebookme.server.notification.preferences.exception.NotificationPreferenceNotFoundException;
import com.pleasebookme.server.notification.queue.exception.NotificationQueueNotFoundException;
import com.pleasebookme.server.notification.template.exception.DuplicateNotificationTemplateException;
import com.pleasebookme.server.notification.template.exception.NotificationTemplateNotFoundException;
import com.pleasebookme.server.organization.membership.exception.DuplicateMembershipException;
import com.pleasebookme.server.organization.membership.exception.MembershipNotFoundException;
import com.pleasebookme.server.organization.membershiprole.exception.DuplicateMembershipRoleException;
import com.pleasebookme.server.organization.membershiprole.exception.MembershipRoleNotFoundException;
import com.pleasebookme.server.organization.organizations.exception.DuplicateOrganizationException;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.profile.exception.DuplicateProfileException;
import com.pleasebookme.server.organization.profile.exception.ProfileNotFoundException;
import com.pleasebookme.server.resource.assignment.exception.ResourceAssignmentNotFoundException;
import com.pleasebookme.server.resource.attribute.exception.DuplicateResourceAttributeException;
import com.pleasebookme.server.resource.attribute.exception.ResourceAttributeNotFoundException;
import com.pleasebookme.server.resource.calendar.exception.ResourceCalendarNotFoundException;
import com.pleasebookme.server.resource.maintenance.exception.ResourceMaintenanceNotFoundException;
import com.pleasebookme.server.resource.overrides.exception.ResourceOverrideNotFoundException;
import com.pleasebookme.server.resource.pricing.exception.ResourcePricingNotFoundException;
import com.pleasebookme.server.resource.resources.exception.DuplicateResourceException;
import com.pleasebookme.server.resource.type.exception.ResourceTypeNotFoundException;
import com.pleasebookme.server.tenant.domain.exception.DuplicateTenantDomainException;
import com.pleasebookme.server.tenant.domain.exception.TenantDomainNotFoundException;
import com.pleasebookme.server.tenant.ecosystem.exception.DuplicateEcosystemException;
import com.pleasebookme.server.tenant.ecosystem.exception.EcosystemNotFoundException;
import com.pleasebookme.server.tenant.plan.exception.DuplicateTenantPlanException;
import com.pleasebookme.server.tenant.plan.exception.TenantPlanNotFoundException;
import com.pleasebookme.server.tenant.tenants.exception.DuplicateTenantException;
import com.pleasebookme.server.tenant.tenants.exception.TenantNotFoundException;
import com.pleasebookme.server.widget.widgetorigin.exception.DuplicateWidgetOriginException;
import com.pleasebookme.server.widget.widgetorigin.exception.WidgetOriginNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
        UserNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserException(
        DuplicateUserException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameAlreadyExistException(
        UsernameAlreadyExistException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserEmailAlreadyExistException(
        UserEmailAlreadyExistException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserPhoneNumberAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserPhoneNumberAlreadyExistException(
        UserPhoneNumberAlreadyExistException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserPasswordNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserPasswordNotFoundException(
        UserPasswordNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserPasswordException(
        DuplicateUserPasswordException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFoundException(
        RoleNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRoleException(
        DuplicateRoleException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissionNotFoundException(
        PermissionNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicatePermissionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicatePermissionException(
        DuplicatePermissionException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserRoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserRoleNotFoundException(
        UserRoleNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserRoleException(
        DuplicateUserRoleException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RolePermissionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRolePermissionNotFoundException(
        RolePermissionNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRolePermissionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRolePermissionException(
        DuplicateRolePermissionException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFoundException(
        AccountNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAccountException(
        DuplicateAccountException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleApiKeyNotFoundException(
        ApiKeyNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateApiKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateApiKeyException(
        DuplicateApiKeyException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenNotFoundException(
        RefreshTokenNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRefreshTokenException(
        DuplicateRefreshTokenException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceNotFoundException(
        ServiceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateServiceException(
        DuplicateServiceException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrganizationNotFoundException(
        OrganizationNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateOrganizationException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateOrganizationException(
        DuplicateOrganizationException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProfileNotFoundException(
        ProfileNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateProfileException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateProfileException(
        DuplicateProfileException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleScheduleNotFoundException(
        ScheduleNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DestinationCalendarNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDestinationCalendarNotFoundException(
        DestinationCalendarNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAvailabilityNotFoundException(
        AvailabilityNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingPolicyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingPolicyNotFoundException(
        BookingPolicyNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingNotFoundException(
        BookingNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AttendeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAttendeeNotFoundException(
        AttendeeNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SelectedSlotNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSelectedSlotNotFoundException(
        SelectedSlotNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateSelectedSlotException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSelectedSlotException(
        DuplicateSelectedSlotException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OutOfOfficeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOutOfOfficeNotFoundException(
        OutOfOfficeNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DestinationSheetsNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDestinationSheetsNotFoundException(
        DestinationSheetsNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantNotFoundException(
        TenantNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateTenantException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateTenantException(
        DuplicateTenantException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(WidgetNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWidgetNotFoundException(
        WidgetNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateWidgetException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateWidgetException(
        DuplicateWidgetException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EcosystemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEcosystemNotFoundException(
        EcosystemNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEcosystemException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEcosystemException(
        DuplicateEcosystemException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TenantPlanNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantPlanNotFoundException(
        TenantPlanNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateTenantPlanException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateTenantPlanException(
        DuplicateTenantPlanException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TenantDomainNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantDomainNotFoundException(
        TenantDomainNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateTenantDomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateTenantDomainException(
        DuplicateTenantDomainException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(WidgetOriginNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWidgetOriginNotFoundException(
        WidgetOriginNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateWidgetOriginException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateWidgetOriginException(
        DuplicateWidgetOriginException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceTypeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceTypeNotFoundException(
        ResourceTypeNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceEntityNotFoundException(
        com.pleasebookme.server.resource.resources.exception.ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
        DuplicateResourceException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourcePricingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourcePricingNotFoundException(
        ResourcePricingNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAssignmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAssignmentNotFoundException(
        ResourceAssignmentNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceCalendarNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceCalendarNotFoundException(
        ResourceCalendarNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceMaintenanceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceMaintenanceNotFoundException(
        ResourceMaintenanceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceOverrideNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceOverrideNotFoundException(
        ResourceOverrideNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAttributeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAttributeNotFoundException(
        ResourceAttributeNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceAttributeException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResourceAttributeException(
        DuplicateResourceAttributeException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFoundException(
        CustomerNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateCustomerException(
        DuplicateCustomerException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomerTagNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerTagNotFoundException(
        CustomerTagNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateCustomerTagException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateCustomerTagException(
        DuplicateCustomerTagException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomerSourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerSourceNotFoundException(
        CustomerSourceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerNoteNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNoteNotFoundException(
        CustomerNoteNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerActivityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerActivityNotFoundException(
        CustomerActivityNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotificationChannelNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationChannelNotFoundException(
        NotificationChannelNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateNotificationChannelException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateNotificationChannelException(
        DuplicateNotificationChannelException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotificationTemplateNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationTemplateNotFoundException(
        NotificationTemplateNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateNotificationTemplateException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateNotificationTemplateException(
        DuplicateNotificationTemplateException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFoundException(
        NotificationNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotificationQueueNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationQueueNotFoundException(
        NotificationQueueNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotificationDeliveryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationDeliveryNotFoundException(
        NotificationDeliveryNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotificationPreferenceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationPreferenceNotFoundException(
        NotificationPreferenceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateNotificationPreferenceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateNotificationPreferenceException(
        DuplicateNotificationPreferenceException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuditEventNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAuditEventNotFoundException(
        AuditEventNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuditActorNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAuditActorNotFoundException(
        AuditActorNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateAuditActorException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAuditActorException(
        DuplicateAuditActorException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuditResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAuditResourceNotFoundException(
        AuditResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuditChangeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAuditChangeNotFoundException(
        AuditChangeNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MembershipNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMembershipNotFoundException(
        MembershipNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateMembershipException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateMembershipException(
        DuplicateMembershipException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MembershipRoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMembershipRoleNotFoundException(
        MembershipRoleNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateMembershipRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateMembershipRoleException(
        DuplicateMembershipRoleException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenExpiredException(
        TokenExpiredException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenExpiredException(
        RefreshTokenExpiredException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenRevokedException(
        RefreshTokenRevokedException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(WidgetOriginMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleWidgetOriginMismatchException(
        WidgetOriginMismatchException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(WidgetNotActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleWidgetNotActiveException(
        WidgetNotActiveException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(WidgetExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleWidgetExpiredException(
        WidgetExpiredException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OAuthConnectionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOAuthConnectionNotFoundException(
        OAuthConnectionNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateOAuthConnectionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateOAuthConnectionException(
        DuplicateOAuthConnectionException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SyncJobNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSyncJobNotFoundException(
        SyncJobNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DestinationDriveNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDestinationDriveNotFoundException(
        DestinationDriveNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidGoogleIdTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidGoogleIdTokenException(
        InvalidGoogleIdTokenException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(GoogleAccountEmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleGoogleAccountEmailNotVerifiedException(
        GoogleAccountEmailNotVerifiedException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidSessionHandoffException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSessionHandoffException(
        InvalidSessionHandoffException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(WidgetBadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleWidgetBadCredentialsException(
        WidgetBadCredentialsException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthenticatedException(
        UnauthenticatedException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenActorException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenActorException(
        ForbiddenActorException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(
        AuthorizationDeniedException exception,
        HttpServletRequest request
    ) {
        HttpStatus status = "OUT_OF_SCOPE".equals(exception.decision().code())
            ? HttpStatus.NOT_FOUND
            : HttpStatus.FORBIDDEN;

        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.decision().code(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(OAuthConnectionAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleOAuthConnectionAccessDeniedException(
        OAuthConnectionAccessDeniedException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GoogleTokenExchangeException.class)
    public ResponseEntity<ApiErrorResponse> handleGoogleTokenExchangeException(
        GoogleTokenExchangeException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(GoogleTokenRefreshException.class)
    public ResponseEntity<ApiErrorResponse> handleGoogleTokenRefreshException(
        GoogleTokenRefreshException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(
            error,
            exception.isInvalidGrant() ? HttpStatus.CONFLICT : HttpStatus.BAD_GATEWAY
        );
    }
}
