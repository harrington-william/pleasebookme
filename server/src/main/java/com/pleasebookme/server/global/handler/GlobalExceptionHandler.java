package com.pleasebookme.server.global.handler;

import com.pleasebookme.server.auth.account.exception.AccountNotFoundException;
import com.pleasebookme.server.auth.account.exception.DuplicateAccountException;
import com.pleasebookme.server.auth.apikey.exception.ApiKeyNotFoundException;
import com.pleasebookme.server.auth.apikey.exception.DuplicateApiKeyException;
import com.pleasebookme.server.auth.password.exception.DuplicateUserPasswordException;
import com.pleasebookme.server.auth.password.exception.UserPasswordNotFoundException;
import com.pleasebookme.server.auth.permission.exception.DuplicatePermissionException;
import com.pleasebookme.server.auth.permission.exception.PermissionNotFoundException;
import com.pleasebookme.server.auth.role.exception.DuplicateRoleException;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.rolepermission.exception.DuplicateRolePermissionException;
import com.pleasebookme.server.auth.rolepermission.exception.RolePermissionNotFoundException;
import com.pleasebookme.server.auth.refreshtoken.exception.DuplicateRefreshTokenException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenNotFoundException;
import com.pleasebookme.server.auth.user.exception.DuplicateUserException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
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
import com.pleasebookme.server.global.exception.ResourceNotFoundException;
import com.pleasebookme.server.integration.calendar.exception.DestinationCalendarNotFoundException;
import com.pleasebookme.server.integration.sheets.exception.DestinationSheetsNotFoundException;
import com.pleasebookme.server.organization.organizations.exception.DuplicateOrganizationException;
import com.pleasebookme.server.organization.organizations.exception.OrganizationNotFoundException;
import com.pleasebookme.server.organization.profile.exception.DuplicateProfileException;
import com.pleasebookme.server.organization.profile.exception.ProfileNotFoundException;
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
import com.pleasebookme.server.widget.widgets.exception.DuplicateWidgetException;
import com.pleasebookme.server.widget.widgets.exception.WidgetNotFoundException;
import com.pleasebookme.server.global.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
