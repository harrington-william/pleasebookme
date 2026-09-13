The platform is designed as a centralized, API-first reservation infrastructure that serves multiple business tenants through reusable booking components. Rather than deploying isolated booking systems for each customer, all businesses share a centralized backend platform while maintaining complete tenant isolation through authentication, authorization, and resource ownership boundaries.

The centralized server distributes **Booking Widget** and **Dedicated Website** with credentials to registered tenants. It acts like a global engine, which provides a high-quality private services system including, booking, analytics, and payment (in near future).

The architecture follows a layered infrastructure model that separates user-facing interfaces, edge infrastructure, application services, data persistence, operational integrations, analytics pipelines, and deployment automation. Each layer has a well-defined responsibility, allowing the platform to evolve incrementally without introducing unnecessary coupling.

At a high level, the architecture consists of the following major layers:

```

Presentation Layer

↓

Edge Infrastructure

↓

Application Layer

↓

Platform Services

↓

Data Layer

↓

Integration Layer

↓

Analytics Layer

↓

Operations Layer

```

---
# 1. Presentation Layer

The presentation layer represents every interface through which customers interact with the reservation platform.

There are currently two primary entry points.

## Embedded Booking Widget

The embedded booking widget is the primary public interface exposed to end users. The widget is designed to be embedded into lightweight marketing websites or future third-party platforms.

The widget itself contains very little business logic. Its responsibility is limited to:

- Displaying availability

- Collecting booking information

- Authenticating widget identity

- Sending booking requests

- Rendering booking responses

Every booking operation is delegated to the centralized backend platform.

---
## Business Website

Each business receives a lightweight marketing website that introduces the business while embedding the booking widget.
  
The website is intentionally simple because the platform is not intended to become a website builder. Instead, it acts as a customer acquisition channel that drives visitors into the reservation infrastructure.

---
## Business Dashboard

Business owners interact with the platform through an authenticated dashboard.  

The dashboard communicates with the same backend APIs as the booking widget and provides operational capabilities such as:

- Resource management

- Booking management

- Availability configuration

- Customer management

- Analytics

- Notification configuration

- Google Calendar synchronization

---
# 2. Edge Infrastructure

Every incoming request enters the platform through the edge infrastructure.

```

Internet

↓

Cloudflare

↓

Nginx Reverse Proxy

↓

Load Balancer

```

---
## Cloudflare

Cloudflare serves as the public entry point of the platform.

Responsibilities include:

- DNS

- TLS termination

- DDoS protection

- Web Application Firewall (WAF)

- Edge caching

- Bot mitigation

- Traffic filtering

Cloudflare significantly reduces the attack surface exposed to the backend infrastructure.

---
## Nginx Reverse Proxy

Nginx performs application-level request processing.

Responsibilities include:

- Reverse proxy

- Request routing

- CORS validation

- Widget origin validation

- Request size limitations

- Rate limiting

- Security headers

- Static asset delivery

  

This layer acts as the first application-aware security boundary.

---
## Load Balancer

The load balancer distributes incoming traffic across multiple application instances.

This enables:

- Horizontal scaling

- Rolling deployments

- High availability

- Fault tolerance

---

# 3. Application Layer

The application layer hosts the centralized reservation platform.

Initially, the platform is implemented as a **modular monolith** using Spring Boot.

Multiple application instances (Pods) run identical deployments.

```

API Gateway

↓

Centralized Booking Platform

```

---
## API Gateway

Each application instance exposes a unified API Gateway.

Responsibilities include:

- Authentication

- Authorization

- Widget token validation

- Request validation

- Rate limiting

- API versioning

- Internal routing

Although currently deployed together with the application, the gateway prepares the platform for future service decomposition.

---

## Centralized Reservation Platform

The centralized backend contains all business logic.

Core responsibilities include:

- Reservation management

- Availability computation

- Booking policies

- Resource allocation

- Customer management

- Tenant isolation

- Authentication

- Authorization

- Audit logging

- Business workflows

Every client—including widgets, dashboards, and future mobile applications—communicates exclusively through this platform.

---
# 4. Platform Services

Several infrastructure services support the core platform.
## Redis

Redis provides high-speed in-memory storage for transient platform data.

Typical use cases include:

- Session storage

- Availability caching

- Widget caching

- Rate limiting

- Temporary booking locks

- Frequently accessed configuration

Redis reduces database load while improving response latency.

---

## PostgreSQL

PostgreSQL serves as the authoritative system of record.

Major schemas include:

```

Identity

  

Tenant

  

Reservation

  

Customer

  

Resource

  

Booking Policy

  

Integration

  

Analytics

  

Audit

```

The database stores only durable business data while transient operational data remains inside Redis.

---
# 5. Integration Layer

The integration layer connects the reservation platform with external providers.

This layer is intentionally separated from transactional booking logic because integrations are inherently asynchronous and failure-prone.

Current planned integrations include:
## Google OAuth2

Provides secure delegated authorization.

Business owners grant access to:

- Google Calendar

- Google Workspace

without exposing account credentials.

---
## Google Calendar Synchronization

Synchronizes reservations between the booking platform and external calendars.

Synchronization includes:

- Booking creation

- Booking updates

- Booking cancellation

---

## SMS / Zalo Integration

Provides outbound customer communication.

Typical events include:

- Booking confirmation

- Reminder messages

- Cancellation notifications

- Schedule changes

---
## Notification Service

The Notification Service consumes business events and determines:

- Delivery channel

- Message template

- Localization

- Retry policy

- Provider selection

The service is independent of the booking workflow.

---
# 6. Event Infrastructure

As the platform evolves, asynchronous communication becomes increasingly important.

Apache Kafka functions as the platform's event backbone.

Rather than allowing application modules to communicate directly, they publish immutable business events.

Typical events include:

```

ReservationCreated

  

ReservationConfirmed

  

ReservationCancelled

  

AvailabilityUpdated

  

CustomerCreated

  

NotificationRequested

```

Consumers subscribe independently without increasing coupling.

Future consumers include:

- Notification Service

- Analytics Service

- Audit Pipeline

- Automation Engine

---
# 7. Analytics Platform

Operational data is gradually transformed into analytical data.

```

PostgreSQL

↓

ETL

↓

Data Warehouse

↓

Analytics Service

↓

Dashboard

```

---
## ETL Pipeline

The ETL pipeline extracts transactional data, transforms it into analytical models, and loads it into the data warehouse.

Transactional databases remain optimized for operational workloads while analytical workloads execute independently.

---

## Data Warehouse

The warehouse stores historical business data optimized for reporting.

Examples include:

- Booking trends

- Revenue reports

- Resource utilization

- Customer retention

- Cancellation rates

---
## Analytics Service

The Analytics Service exposes reporting APIs used by internal dashboards.

This separation prevents expensive analytical queries from impacting production booking performance.

---
# 8. Deployment Pipeline

The platform follows a fully automated CI/CD workflow.

```

Development

↓

Git Repository

↓

Merge to Main

↓

GitHub Actions

↓

Container Registry

↓

Production Deployment

```

Every production deployment is generated automatically from version-controlled source code.

This workflow enables:

- Repeatable deployments

- Automated testing

- Containerized releases

- Rollback capability

- Infrastructure consistency

---
# End-to-End Request Flow

A typical reservation request follows this sequence:

```

Customer

↓

Marketing Website

↓

Embedded Booking Widget

↓

Cloudflare

↓

Nginx Reverse Proxy

↓

Load Balancer

↓

API Gateway

↓

Reservation Platform

↓

Redis Cache

↓

PostgreSQL

↓

Business Event

↓

Kafka

↓

Notification Service

↓

Google Calendar / SMS / Zalo

```

Simultaneously, completed reservation events are streamed into the analytical pipeline:

```

PostgreSQL

↓

ETL

↓

Data Warehouse

↓

Analytics Service

↓

Business Dashboard

```

---

# Architectural Philosophy

The architecture intentionally follows an **API-first, platform-oriented design**. Every user interface—including the embedded booking widget, business dashboard, customer portal, and future mobile applications—is treated as a client of the same centralized reservation platform. The backend remains the single source of truth for business rules, reservation policies, resource allocation, and tenant management.

Although the long-term vision includes event-driven services and independently deployable components, the platform is intentionally developed as a **modular monolith** during its early commercial stages. This provides operational simplicity while preserving clear module boundaries for future service extraction. As customer adoption and infrastructure demands grow, individual modules such as Notifications, Analytics, Identity, and Reservation Management can be evolved into independent services without requiring fundamental changes to the core business domain.

This architecture aligns well with your strategic objective of evolving from a booking application into a **reservation infrastructure platform**, where the booking widget is merely one client of a reusable API capable of serving websites, mobile applications, partner systems, and future third-party developers.