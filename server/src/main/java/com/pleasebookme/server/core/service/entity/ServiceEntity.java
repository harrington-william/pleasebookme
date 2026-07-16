package com.pleasebookme.server.core.service.entity;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.core.schedule.entity.ScheduleEntity;
import com.pleasebookme.server.global.enums.Currency;
import com.pleasebookme.server.global.enums.Locale;
import com.pleasebookme.server.organization.organizations.entity.OrganizationEntity;
import com.pleasebookme.server.organization.profile.entity.ProfileEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(
    schema = "core",
    name = "services"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Missing destination calendar & sheets
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger serviceId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "interface_language", nullable = false)
    private Locale interfaceLanguage = Locale.en;

    @Column(name = "location")
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @Builder.Default
    @Column(name = "period_type", nullable = false, length = 50)
    private String periodType = "UNLIMITED";

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone = "Australia/Sydney";

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.USD;

    @Builder.Default
    @Column(name = "requires_confirmation", nullable = false)
    private Boolean requiresConfirmation = false;

    @Builder.Default
    @Column(name = "disable_cancelling", nullable = false)
    private Boolean disableCancelling = false;

    @Builder.Default
    @Column(name = "disable_rescheduling", nullable = false)
    private Boolean disableRescheduling = false;

    @Column(name = "success_redirect_url")
    private String successRedirectUrl;

    @Builder.Default
    @Column(name = "is_instant_service", nullable = false)
    private Boolean isInstantService = false;

    @Column(name = "max_active_booking_per_booker")
    private Integer maxActiveBookingPerBooker;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Column(name = "destination_calendar_id")
    private BigInteger destinationCalendarId;

    @Column(name = "destination_sheets_id")
    private BigInteger destinationSheetsId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
