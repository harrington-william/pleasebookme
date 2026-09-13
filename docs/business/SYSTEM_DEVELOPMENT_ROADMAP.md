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

## Production Platform v1.0 — Commercial Foundation (In Progress)

This phase marks the transition from MVP development into the first production-grade version of the platform.

The primary objective is to deliver a commercially deployable centralized booking platform capable of serving real customers while establishing the operational foundation required for long-term infrastructure growth.

Unlike previous planning, this phase does not include a backend migration away from Spring Boot. Instead, the backend architecture continues to evolve as a stable API-first platform while NextJS becomes the standard frontend framework for all customer-facing applications.

Major objectives include:

- Production deployment on AWS
- Nginx reverse proxy and traffic routing
- Automated CI/CD pipelines
- Production environment management
- HTTPS and operational security hardening
- Redis caching for performance optimization
- Google OAuth2 authentication
- Official business management dashboard
- Embedded booking widget
- Public booking website
- Multi-tenant platform expansion
- Integration schemas
- Expanded domain schemas
- Reservation infrastructure normalization
- API-first platform architecture

This phase also introduces operational dashboards that allow business owners to configure booking policies, resources, availability, schedules, customers, and reservations through a production-ready management interface.

By the completion of this phase, the platform should operate as a centralized SaaS application capable of onboarding real businesses and processing production traffic.

---

# Platform Phases

## Platform v2.0 — Infrastructure Integration

Platform v2.0 focuses on transforming the centralized application into an integration-ready operational platform.

The primary objective is to introduce asynchronous communication patterns, external service integrations, and infrastructure components that prepare the system for larger operational workloads.

Major additions include:

- Apache Kafka message streaming
- Cloudflare edge entry point
- API Gateway
- SMS notification providers
- Email notification services
- Google Calendar synchronization
- External integration architecture
- Analytics schema
- Operational event streaming
- Service communication patterns
- CI/CD optimization
- Infrastructure deployment refinement

During this phase, notifications become independent infrastructure capabilities rather than synchronous application logic. Business events begin producing platform events that can be consumed by downstream services without increasing coupling.

The completion of Platform v2.0 establishes the foundation for an event-driven architecture while maintaining the simplicity of the centralized production deployment.

## Platform v3.0 — Infrastructure Optimization & Analytics

Platform v3.0 represents the transition from application engineering into infrastructure engineering.

The primary objective is operational scalability, data engineering, observability, and analytical capability.

Major objectives include:

- ETL pipelines
- Data warehouse
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

After Platform v3.0, the project enters continuous production evolution.

At this stage, the platform is no longer discovering fundamental architectural concepts. Instead, development focuses on infrastructure maturity, commercial expansion, operational excellence, developer experience, and ecosystem growth.

Future engineering initiatives include:

- Public Developer API
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

The long-term objective is to evolve beyond a booking application into an API-first reservation infrastructure platform. Every interface—including the embedded booking widget, business dashboard, customer portal, mobile applications, and future third-party integrations—operates as a client of the same centralized backend infrastructure. This architecture enables the platform to scale from serving local Vietnamese businesses to becoming a reusable reservation platform capable of supporting multiple industries, international regions, and external software ecosystems.