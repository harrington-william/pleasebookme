@AGENTS.md

# Business Name: **Please Book Me**
# Current Phase: We are currently building PLATFORM V1. The first ever production-grade product.

---

# Business Overview

Please Book Me is a **booking infrastructure** designed to provide reusable reservation capabilities for small-to-medium service businesses. Rather than focusing on building custom enterprise websites, the primary objective is to create a centralized reservation platform that exposes standardized booking capabilities through APIs, embedded widgets, management dashboards, and future developer integrations.

Many businesses in Vietnam that already rely on social media but do not yet have a proper website, booking system, or normalized operational workflow. Specifically, many maturing businesses spend a massive amount of money on building social media channels, Facebook Ads, and Google ads. However, they do not have any public interactable website, which allow their customer to maneuver with. For example, a small barbershop spend a lot of money on his TikTok channel, but users can not schedule appointment or check for the barbershop info such as services, story, and updates.

The initial commercial offering combines lightweight template-based marketing websites with a reusable embedded booking widget powered by a centralized backend platform. These websites intentionally remain minimal because their primary purpose is customer acquisition, business presentation, and service discovery rather than acting as complex business applications. Every website functions as a thin presentation layer that consumes the same booking infrastructure used throughout the platform.

The embedded booking widget represents the first client of the platform rather than the platform itself. It communicates with a centralized reservation engine responsible for availability computation, scheduling orchestration, conflict detection, booking lifecycle management, tenant isolation, and operational workflows. This architecture allows every business to share the same underlying infrastructure while maintaining complete logical isolation of data, resources, customers, and operational policies.

The system enables the same reservation engine to support a wide variety of business models without requiring separate implementations. A salon appointment, a hotel room reservation, a meeting room booking, a co-working desk reservation, a sports court rental, or an equipment rental are all represented as reservations that allocate one or more resources for a specific time interval under configurable booking policies. Fixed-duration appointments, customer-defined reservation periods, and hybrid scheduling models are treated as policy variations rather than different application architectures.

Over time, the platform is intended to evolve from a booking application into a complete reservation infrastructure ecosystem. Future capabilities include developer APIs, SDKs, webhooks, notification orchestration, analytics, billing, auditability, automation workflows, and partner integrations. In this model, the booking widget is no longer considered the core product; instead, it becomes one of many interfaces built on top of a centralized reservation platform that can be integrated into websites, mobile applications, AI agents, and third-party business systems.

# Business Market

The target market remains small businesses that currently manage reservations manually through social media messaging, SMS, phone calls, or spreadsheets. By providing production-ready reservation infrastructure through standardized APIs and reusable user interfaces, the platform enables these businesses to adopt modern scheduling capabilities without investing in custom software development. As the platform matures, the same infrastructure can serve increasingly sophisticated operational requirements while remaining extensible enough to support new industries, regional expansion, and future platform capabilities.

## Market Expected Expansion

The original target market is Vietnam, introduce booking infrastructure to businesses operating entirely within the UTC +7 time zone. Then gradually expand to Australian market before becoming a stable regional infrastructure (Maybe Asia / Pacific).

# Long-term Orientation (High-level)

The long-term strategic direction of the business has evolved from a Vietnam-only platform into a **regionally** scalable appointment infrastructure product. The initial operational phase will remain focused entirely on Vietnam for approximately 2–3 years in order to stabilize the infrastructure, normalize operational workflows, validate the business model, and mature the engineering architecture. After the Vietnam operational phase becomes stable, the system is planned to expand into the Australian market, which introduces time zone complexity from UTC+8 to UTC+10 and eventually requires more sophisticated temporal abstractions, time zone conversions, and infrastructure scalability.

The roadmap is intentionally divided into multiple MVP phases because the platform is treated as a serious infrastructure engineering project rather than a simple CRUD application. Each MVP phase isolates one major architectural concern so that the system evolves through controlled complexity growth instead of attempting premature enterprise-scale engineering.

---

# Australian Orientation

This stage is not intended to discover core architectural concepts anymore because the earlier MVP phases already isolate and validate each major subsystem independently. Instead, the production-grade phase focuses on operational stabilization, commercial scaling, infrastructure hardening, deployment reliability, tenant scalability, and regional expansion.

The time zone expansion strategy is intentionally staged and controlled. During the Vietnam operational phase, the platform will initially operate entirely in UTC+7 in order to simplify temporal complexity and accelerate infrastructure maturity. However, the architecture is intentionally designed from the beginning to avoid irreversible time zone assumptions. All temporal operations are planned around canonical UTC storage internally even while frontend displays remain localized to Vietnam time. This means booking timestamps, availability windows, audit events, and operational records should eventually be normalized into UTC-based storage and converted dynamically for presentation layers.

The future Australian expansion significantly changes the temporal complexity of the system because Australia introduces multiple time zones and daylight saving behavior. For that reason, time zone logic is treated as a future infrastructure concern that must be prepared architecturally early, even if operationally postponed. The platform must eventually support timezone-aware APIs, centralized temporal conversion services, canonical UTC storage, region-aware slot generation, and localized presentation layers. The engineering philosophy is to keep operational complexity simple during the Vietnam phase while preserving architectural flexibility for future regional expansion.

The roadmap is intentionally locked to preserve engineering discipline. One of the major risks in infrastructure-heavy projects is endless architectural redesign and premature optimization. Therefore, each MVP phase is isolated with strict architectural objectives, and feature expansion is intentionally constrained in order to maintain forward momentum. The project philosophy prioritizes systems understanding, operational maturity, infrastructure normalization, and architectural clarity over rapid feature accumulation.

# Conventions

## MVP

> MVPs are isolated-domain testing products, they are primarily used to validate a major application’s capability such as core domain, auth domain, and check if the tech-stack fits the project.
> 

## PLATFORM

> Platform phases represent production-grade application evolution roadmap, which mean scaling an application that ALREADY deployed to AWS Cloud Provider. Platform phases emphasize that they are not testing products, they are serving platform, and illustrate the scaling strategy.
> 

---

# MVP Phases

## MVP v1 - Completed

MVP v1 was the foundational booking-engine phase and has already been completed successfully. The purpose of MVP v1 was not to build a production-ready application, but to deeply understand slot generation logic, booking lifecycle mechanics, temporal conflict handling, schedule computation, and availability orchestration. The MVP v1 system was implemented using Spring Boot for the backend and React + Vite + TypeScript for the frontend because those technologies were already familiar. The core technical objective of MVP v1 was to build and understand the booking domain schema, especially the interaction between EventType, Schedule, Availability, SelectedSlots, Booking, and Attendee models. This phase focused heavily on learning how booking systems actually compute available slots dynamically rather than storing generated slots statically. The most important outcome of MVP v1 was discovering that the real complexity of booking systems exists in temporal orchestration, concurrency control, and availability computation rather than in frontend rendering.

## MVP v2 - Completed

MVP v2 is the current active phase. The objective of MVP v2 is to normalize identity and security architecture while reusing the entire booking engine from MVP v1. This phase introduces authentication systems, RBAC, attribute-based authorization concepts, tenant isolation, permission boundaries, and security-oriented database schemas. The system remains built with Spring Boot and NextJS + TypeScript because the purpose of this phase is not framework migration, but security flow understanding. This phase also acts as a transition period during which TypeScript knowledge and NextJS knowledge are developed progressively. MVP v2 represents the transformation from a scheduling prototype into a real platform infrastructure foundation because the system now begins handling ownership, authorization, resource isolation, and protected operational workflows. The technical focus of MVP v2 is not feature expansion, but normalization of backend security architecture and preparation for future scalable platform behavior.

## Production Platform v1.0.0 — Commercial Foundation (In Progress)

This phase marks the transition from MVP development into the first production-grade version of the platform.

The primary objective is to deliver a commercially deployable centralized booking platform capable of serving real customers while establishing the operational foundation required for long-term infrastructure growth.

Unlike previous planning, this phase does not include a backend migration away from Spring Boot. Instead, the backend architecture continues to evolve as a stable API-first platform while NextJS becomes the standard frontend framework for all customer-facing applications.

Major objectives include:

- Production deployment on AWS
- Automated CI/CD pipelines
- Production environment management
- HTTPS and operational security hardening
- Redis caching for performance optimization
- Google OAuth2 authentication
- Official business management dashboard
- Embedded booking widget
- Public booking website
- Multi-tenant platform expansion
- Google Calendar synchronization
- Google Sheets synchronization
- Integration schemas
- Expanded domain schemas
- Reservation infrastructure normalization
- API-first platform architecture

Implemented schemas:

- core
- auth
- audit
- tenant
- widget
- resource
- notification
- integration
- customer
- organization

Temporarily excluded schemas:

- webhook
- billing
- analytics

This phase also introduces operational dashboards that allow business owners to configure booking policies, resources, availability, schedules, customers, and reservations through a production-ready management interface.

By the completion of this phase, the platform should operate as a centralized SaaS application capable of onboarding real businesses and processing production traffic.

---

# Platform Phases

## Platform v2.0.0 — Infrastructure Integration

Platform v2.0.0 focuses on transforming the centralized application into an integration-ready operational platform.

The primary objective is to introduce asynchronous communication patterns, external service integrations, and infrastructure components that prepare the system for larger operational workloads.

Major additions include:

- Apache Kafka message streaming
- Nginx reverse proxy for traffic routing
- Cloudflare edge entry point
- API Gateway
- SMS notification providers
- Email notification services
- External integration architecture
- Analytics schema
- Operational event streaming
- Service communication patterns
- CI/CD optimization
- Infrastructure deployment refinement

Implemented schemas:

- billing
- analytics
- expand customer schema

During this phase, notifications become independent infrastructure capabilities rather than synchronous application logic. Business events begin producing platform events that can be consumed by downstream services without increasing coupling.

The completion of Platform v2.0.0 establishes the foundation for an event-driven architecture while maintaining the simplicity of the centralized production deployment.

## Platform v3.0.0 — Infrastructure Optimization & Analytics

Platform v3.0.0 represents the transition from application engineering into infrastructure engineering.

The primary objective is operational scalability, data engineering, observability, and analytical capability.

Major objectives include:

- Analytics service
- Operational dashboards
- Business intelligence
- Queue optimization
- Event processing optimization
- Observability
- Distributed logging
- Metrics collection
- Infrastructure monitoring
- Performance optimization
- Tenant scalability
- Australian timezone preparation
- Canonical UTC temporal architecture

The booking platform itself remains functionally stable during this phase. Engineering effort shifts toward improving platform operations, processing efficiency, deployment reliability, infrastructure observability, and analytical capabilities.

This phase also introduces the architectural preparation required for international expansion. Although the platform initially operates exclusively within Vietnam (UTC+7), all temporal operations continue evolving toward canonical UTC storage, centralized timezone conversion services, region-aware scheduling, and localized presentation layers. These capabilities ensure that future expansion into Australia and other regions can be achieved without redesigning the reservation engine.

---

# Production Evolution

After Platform v3.0.0, the project enters continuous production evolution.

At this stage, the platform is no longer discovering fundamental architectural concepts. Instead, development focuses on infrastructure maturity, commercial expansion, operational excellence, developer experience, and ecosystem growth.

Future engineering initiatives include:

- Public Developer API
- ETL pipelines
- Data warehouse
- API key management
- SDK development
- Webhook platform
- Partner integrations
- Multi-region deployment
- Multi-database strategies
- Advanced analytics
- AI-assisted operational automation
- Global timezone support
- Infrastructure resiliency
- Disaster recovery
- Regional expansion

Implemented schemas:

- webhook

The long-term objective is to evolve beyond a booking application into an API-first reservation infrastructure platform. Every interface—including the embedded booking widget, business dashboard, customer portal, mobile applications, and future third-party integrations—operates as a client of the same centralized backend infrastructure. This architecture enables the platform to scale from serving local Vietnamese businesses to becoming a reusable reservation platform capable of supporting multiple industries, international regions, and external software ecosystems.

# Folder Structure


