# Event Ticket Platform Backend - Technical Documentation

> **Version**: 1.0.0  
> **Last Updated**: January 2026  
> **Platform**: Spring Boot 3.5 / Java 21

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture Overview](#2-architecture-overview)
3. [Technology Stack](#3-technology-stack)
4. [Project Structure](#4-project-structure)
5. [Configuration & Environment](#5-configuration--environment)
6. [Database Design](#6-database-design)
7. [API Documentation](#7-api-documentation)
8. [Business Logic Flow](#8-business-logic-flow)
9. [Security](#9-security)
10. [Error Handling & Logging](#10-error-handling--logging)
11. [Testing Strategy](#11-testing-strategy)
12. [Javadoc Requirements](#12-javadoc-requirements)
13. [How to Run the Project](#13-how-to-run-the-project)
14. [Deployment](#14-deployment)
15. [Contribution Guidelines](#15-contribution-guidelines)
16. [Extension Guide](#16-extension-guide)

---

## 1. Project Overview

### 1.1 Project Purpose and Business Problem

The Event Ticket Platform Backend is a RESTful API service that powers an event ticketing system. It solves the following business problems:

- **Event Management**: Organizers can create, publish, and manage events with multiple ticket types
- **Ticket Sales**: Attendees can browse events, purchase tickets, and receive digital tickets with QR codes
- **Ticket Validation**: Staff members can validate tickets at event entry points using QR code scanning
- **Analytics**: Organizers can track sales performance and order statistics for their events

### 1.2 High-Level System Description

This is a backend-only REST API designed to be consumed by frontend applications (web, mobile). The system handles:

- User authentication and authorization via JWT tokens
- Event lifecycle management (draft → published → cancelled)
- Ticket purchasing workflows with inventory management
- QR code generation for digital tickets
- PDF ticket generation for download
- Real-time ticket validation at events

### 1.3 Key Features

| Feature | Description |
|---------|-------------|
| Multi-role User System | Supports ORGANIZER, STAFF, and ATTENDEE roles |
| Event Management | Full CRUD operations with status transitions |
| Ticket Types | Multiple ticket categories per event with independent pricing and inventory |
| QR Code Tickets | Unique QR codes generated for each ticket |
| PDF Generation | Downloadable PDF tickets with embedded QR codes |
| Staff Assignment | Organizers can assign staff to manage event entry |
| Ticket Validation | Staff can scan and validate tickets with audit logging |
| Analytics Dashboard | Sales and order statistics per event |

### 1.4 Target Users

| User Role | Description |
|-----------|-------------|
| **ORGANIZER** | Event creators who manage events, ticket types, and staff |
| **STAFF** | Event workers who validate tickets at entry |
| **ATTENDEE** | End users who browse events and purchase tickets |

---

## 2. Architecture Overview

### 2.1 Overall Architecture

The application follows a **Layered Monolithic Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│              (Web App, Mobile App, Third-party)                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ HTTP/REST
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                           │
│    ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐   │
│    │AuthController│ │EventController│ │TicketController     │   │
│    └──────────────┘ └──────────────┘ └──────────────────────┘   │
│    ┌──────────────────┐ ┌────────────────┐ ┌────────────────┐   │
│    │AnalyticsController│ │PublishedEvent │ │TicketValidation│   │
│    │                  │ │Controller     │ │Controller      │   │
│    └──────────────────┘ └────────────────┘ └────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                             │
│    ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐   │
│    │ AuthService  │ │ EventService │ │   TicketService      │   │
│    └──────────────┘ └──────────────┘ └──────────────────────┘   │
│    ┌──────────────────┐ ┌────────────────┐ ┌────────────────┐   │
│    │AnalyticsService  │ │ QrCodeService  │ │ PdfService     │   │
│    └──────────────────┘ └────────────────┘ └────────────────┘   │
│    ┌──────────────────────────────────────────────────────────┐ │
│    │              TicketValidationService                     │ │
│    └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                           │
│    ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐   │
│    │UserRepository│ │EventRepository│ │  TicketRepository   │   │
│    └──────────────┘ └──────────────┘ └──────────────────────┘   │
│    ┌──────────────────┐ ┌────────────────┐ ┌────────────────┐   │
│    │TicketTypeRepo    │ │TicketOrderRepo │ │  QrCodeRepo    │   │
│    └──────────────────┘ └────────────────┘ └────────────────┘   │
│    ┌──────────────────┐ ┌────────────────────────────────────┐  │
│    │EventStaffRepo    │ │  TicketValidationAttemptRepo      │  │
│    └──────────────────┘ └────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                             │
│                  Neon DB (PostgreSQL) / H2                      │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Layer Responsibilities

| Layer | Responsibility |
|-------|----------------|
| **Controller** | HTTP request handling, request validation, response formatting, authorization annotations |
| **Service** | Business logic, transaction management, cross-cutting concerns, validation rules |
| **Repository** | Data access, query methods, database operations via Spring Data JPA |
| **Entity** | JPA entities mapping to database tables, relationships, constraints |
| **DTO** | Data transfer objects for API requests and responses |
| **Mapper** | Conversion between entities and DTOs using MapStruct |
| **Config** | Application configuration, security setup, beans definition |
| **Exception** | Custom exceptions and global exception handling |

### 2.3 Dependency Flow

```
Controller → Service → Repository → Entity
     ↓           ↓
    DTO      Mapper
```

**Rules:**
- Controllers depend on Services (interfaces)
- Services depend on Repositories (interfaces)
- DTOs are used at Controller boundaries
- Mappers bridge DTOs and Entities
- Entities have no dependencies on other layers

### 2.4 Architectural Decisions and Trade-offs

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| Layered Monolith | Simplicity, easier deployment, sufficient for current scale | Less scalability than microservices |
| Interface-based Services | Testability, flexibility to swap implementations | Additional boilerplate |
| DTOs for API | Decouples internal model from API contract | More mapping code required |
| PostgreSQL Enums | Type safety at DB level, self-documenting schema | Less portable across databases |
| JWT Stateless Auth | Scalable, no session storage needed | Token revocation complexity |

---

## 3. Technology Stack

### 3.1 Core Technologies

| Technology | Version | Rationale |
|------------|---------|-----------|
| **Java** | 21 (Temurin) | LTS release with modern features (records, pattern matching, virtual threads ready) |
| **Spring Boot** | 3.5.0 | Latest stable version with autoconfiguration, embedded server, production-ready features |
| **Maven** | 3.9+ | Industry standard build tool, excellent IDE support, mature ecosystem |

### 3.2 Spring Modules

| Module | Purpose |
|--------|---------|
| **Spring Web** | REST API development, request mapping, HTTP handling |
| **Spring Data JPA** | Repository abstraction, query methods, JPA integration |
| **Spring Security** | Authentication, authorization, security filters |
| **Spring Validation** | Bean validation with annotations |

### 3.3 Database

| Technology | Purpose |
|------------|---------|
| **Neon DB** | Production database - serverless PostgreSQL, ACID-compliant, excellent JSON support, custom enum types |
| **H2** | Test database - in-memory, fast test execution, no external dependencies |
| **Flyway 11** | Database migrations - version control for schema, repeatable deployments |

### 3.4 Third-Party Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **Lombok** | Latest | Reduces boilerplate (getters, setters, constructors, builders) |
| **MapStruct** | 1.6.3 | Compile-time DTO-Entity mapping, type-safe, fast |
| **jjwt** | 0.12.6 | JWT token generation and validation |
| **ZXing** | 3.5.3 | QR code generation for tickets |
| **iText7** | 9.1.0 | PDF generation for downloadable tickets |
| **springdoc-openapi** | 2.8.4 | OpenAPI 3.0 documentation, Swagger UI |

---

## 4. Project Structure

### 4.1 Package Overview

```
src/main/java/com/project/event_ticket_platform/
├── Application.java              # Spring Boot entry point
├── config/                       # Configuration classes
├── controllers/                  # REST API endpoints
├── dtos/                         # Request/Response objects
├── entities/                     # JPA entities and enums
├── exceptions/                   # Custom exceptions
├── mappers/                      # MapStruct mappers
├── repositories/                 # Spring Data JPA repositories
├── security/                     # Security components (JWT, filters)
├── services/                     # Service interfaces
│   └── impl/                     # Service implementations
└── utils/                        # Utility classes
```

### 4.2 Package Details

#### `config/`
**What goes here:**
- Spring configuration classes (`@Configuration`)
- Bean definitions
- Security configuration
- CORS configuration
- OpenAPI configuration

**What does NOT go here:**
- Business logic
- Entity definitions
- Request handlers

**Naming:** `*Config.java` (e.g., `SecurityConfig.java`, `OpenApiConfig.java`)

#### `controllers/`
**What goes here:**
- REST controllers (`@RestController`)
- Request mapping methods
- Request validation annotations
- Response entity construction

**What does NOT go here:**
- Business logic (delegate to services)
- Direct repository access
- Data transformation logic

**Naming:** `*Controller.java` (e.g., `EventController.java`, `TicketController.java`)

#### `dtos/`
**What goes here:**
- Request DTOs (records preferred)
- Response DTOs (records preferred)
- Validation annotations on request fields

**What does NOT go here:**
- JPA annotations
- Business logic
- Entity relationships

**Naming:** 
- Requests: `*Request.java` (e.g., `CreateEventRequest.java`)
- Responses: `*Response.java` (e.g., `EventResponse.java`)

#### `entities/`
**What goes here:**
- JPA entities (`@Entity`)
- Enums for domain concepts
- Entity relationships (`@OneToMany`, `@ManyToOne`, etc.)
- JPA auditing fields

**What does NOT go here:**
- Business logic
- DTOs
- Controller logic

**Naming:** Singular nouns (e.g., `Event.java`, `Ticket.java`, `User.java`)

#### `exceptions/`
**What goes here:**
- Custom exception classes
- Global exception handler (`@RestControllerAdvice`)
- Exception response DTOs

**What does NOT go here:**
- Business logic
- Generic utility methods

**Naming:** `*Exception.java` or `*NotFoundException.java` (e.g., `EventNotFoundException.java`)

#### `mappers/`
**What goes here:**
- MapStruct mapper interfaces (`@Mapper`)
- Custom mapping methods

**What does NOT go here:**
- Business logic
- Manual mapping code (use MapStruct)

**Naming:** `*Mapper.java` (e.g., `EventMapper.java`, `TicketMapper.java`)

#### `repositories/`
**What goes here:**
- Spring Data JPA repository interfaces
- Custom query methods
- `@Query` annotations for complex queries

**What does NOT go here:**
- Business logic
- Transaction management (handled in service)

**Naming:** `*Repository.java` (e.g., `EventRepository.java`, `TicketRepository.java`)

#### `services/`
**What goes here:**
- Service interfaces defining business operations

**What does NOT go here:**
- Implementation details (use `impl/` subpackage)

**Naming:** `*Service.java` (e.g., `EventService.java`, `TicketService.java`)

#### `services/impl/`
**What goes here:**
- Service implementations (`@Service`)
- Business logic
- Transaction management (`@Transactional`)
- Validation logic
- Coordination between repositories

**What does NOT go here:**
- HTTP handling
- Direct DTO construction (use mappers)

**Naming:** `*ServiceImpl.java` (e.g., `EventServiceImpl.java`)

#### `security/`
**What goes here:**
- JWT utility classes
- Security filters
- Authentication providers
- Custom security components

**What does NOT go here:**
- General business logic
- Non-security utilities

**Naming:** Descriptive names (e.g., `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`)

#### `utils/`
**What goes here:**
- General utility classes
- Helper methods
- Constants

**What does NOT go here:**
- Business logic
- Security-specific utilities (use `security/`)

**Naming:** `*Utils.java` or `*Helper.java`

---

## 5. Configuration & Environment

### 5.1 Main Configuration File

**Location:** `src/main/resources/application.properties`

```properties
# Application
spring.application.name=event-ticket-platform

# Database Connection (Neon DB)
# Option 1: Use DATABASE_URL environment variable (recommended)
spring.datasource.url=${DATABASE_URL}

# Option 2: Configure manually
# spring.datasource.url=jdbc:postgresql://host.neon.tech/database?sslmode=require
# spring.datasource.username=username
# spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Jackson
spring.jackson.mapper.accept-case-insensitive-enums=true

# OpenAPI
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Server
server.port=8080
```

### 5.2 Configuration Properties Explained

| Property | Description |
|----------|-------------|
| `spring.datasource.url` | Neon DB connection URL (can use DATABASE_URL env var) |
| `spring.jpa.hibernate.ddl-auto=validate` | Validates schema against entities (Flyway manages DDL) |
| `spring.flyway.enabled=true` | Enables Flyway migrations on startup |
| `spring.jackson.mapper.accept-case-insensitive-enums=true` | Allows case-insensitive enum parsing in JSON |
| `jwt.secret` | Secret key for JWT signing (must be externalized) |
| `jwt.expiration` | JWT token validity in milliseconds (24 hours default) |

### 5.3 Environment-Specific Configuration

Create profile-specific files:

- `application-dev.properties` - Local development
- `application-test.properties` - Test environment
- `application-prod.properties` - Production

**Test Configuration Example:**
```properties
# H2 in-memory database for tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

### 5.4 Profile Activation

```bash
# Command line
java -jar app.jar --spring.profiles.active=prod

# Environment variable
SPRING_PROFILES_ACTIVE=prod

# In application.properties
spring.profiles.active=dev
```

### 5.5 Secrets Handling

**Required Secrets:**
| Secret | Purpose | How to Set |
|--------|---------|------------|
| `DATABASE_URL` | Neon DB connection string | Environment variable (from Neon dashboard) |
| `JWT_SECRET` | JWT signing key (min 256 bits) | Environment variable |

**Best Practices:**
- Never commit secrets to version control
- Use environment variables or secret management services
- Use different secrets per environment
- Rotate JWT secrets periodically

### 5.6 CORS Configuration

CORS is configured in `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    // ...
}
```

**For Production:** Restrict `allowedOrigins` to specific domains.

---

## 6. Database Design

### 6.1 Database Type and Rationale

**Primary Database:** Neon DB (PostgreSQL-compatible)

**Rationale:**
- Serverless PostgreSQL database with auto-scaling
- ACID compliance for financial transactions (ticket purchases)
- Native support for custom enum types (type safety)
- Excellent performance for read-heavy workloads
- Strong ecosystem and tooling
- Production-ready with proven scalability
- Managed service with automatic backups and point-in-time recovery

**Test Database:** H2 (in-memory)

### 6.2 Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    User     │       │    Event    │       │ TicketType  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │───────│ id (PK)     │
│ email       │  │    │ organizer_id│←──┐   │ event_id(FK)│
│ password    │  │    │ name        │   │   │ name        │
│ first_name  │  │    │ description │   │   │ price       │
│ last_name   │  │    │ location    │   │   │ quantity    │
│ role        │  │    │ start_time  │   │   │ sold_count  │
│ created_at  │  │    │ end_time    │   │   │ created_at  │
│ updated_at  │  │    │ status      │   │   │ updated_at  │
└─────────────┘  │    │ created_at  │   │   └─────────────┘
       │         │    │ updated_at  │   │          │
       │         │    └─────────────┘   │          │
       │         │           │          │          │
       │         └───────────┼──────────┘          │
       │                     │                     │
       ▼                     ▼                     ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ EventStaff  │       │ TicketOrder │       │   Ticket    │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ event_id(FK)│←──────│ event_id(FK)│       │ order_id(FK)│
│ user_id(FK) │       │ buyer_id(FK)│       │ type_id(FK) │
│ assigned_at │       │ buyer_email │       │ qr_code_id  │
└─────────────┘       │ buyer_name  │       │ status      │
                      │ status      │       │ created_at  │
                      │ created_at  │       │ updated_at  │
                      │ updated_at  │       └─────────────┘
                      └─────────────┘              │
                                                  │
                                                  ▼
┌─────────────────────────────────┐       ┌─────────────┐
│    TicketValidationAttempt      │       │   QrCode    │
├─────────────────────────────────┤       ├─────────────┤
│ id (PK)                         │       │ id (PK)     │
│ ticket_id (FK)                  │←──────│ ticket_id   │
│ staff_id (FK)                   │       │ code_data   │
│ status (VALID/INVALID/ALREADY)  │       │ status      │
│ method (QR_CODE/MANUAL)         │       │ created_at  │
│ notes                           │       │ updated_at  │
│ validated_at                    │       └─────────────┘
└─────────────────────────────────┘
```

### 6.3 Custom PostgreSQL Enum Types

The database uses custom enum types for type safety:

```sql
CREATE TYPE user_role AS ENUM ('ORGANIZER', 'STAFF', 'ATTENDEE');
CREATE TYPE event_status AS ENUM ('DRAFT', 'PUBLISHED', 'CANCELLED');
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'REFUNDED');
CREATE TYPE ticket_status AS ENUM ('VALID', 'USED', 'CANCELLED', 'EXPIRED');
CREATE TYPE qr_code_status AS ENUM ('ACTIVE', 'USED', 'REVOKED');
CREATE TYPE ticket_validation_status AS ENUM ('VALID', 'INVALID', 'ALREADY_USED');
CREATE TYPE ticket_validation_method AS ENUM ('QR_CODE', 'MANUAL');
```

### 6.4 Table Descriptions

| Table | Purpose |
|-------|---------|
| `users` | Stores user accounts with roles |
| `events` | Event information and status |
| `ticket_types` | Ticket categories with pricing and inventory |
| `ticket_orders` | Purchase transactions |
| `tickets` | Individual tickets linked to orders |
| `qr_codes` | QR code data for tickets |
| `event_staff` | Staff-to-event assignments |
| `ticket_validation_attempts` | Audit log of validation attempts |

### 6.5 Key Constraints and Indexes

**Primary Keys:** All tables use `BIGSERIAL` auto-incrementing IDs.

**Foreign Keys:**
- `events.organizer_id` → `users.id`
- `ticket_types.event_id` → `events.id`
- `ticket_orders.event_id` → `events.id`
- `ticket_orders.buyer_id` → `users.id`
- `tickets.order_id` → `ticket_orders.id`
- `tickets.ticket_type_id` → `ticket_types.id`
- `event_staff.event_id` → `events.id`
- `event_staff.user_id` → `users.id`

**Unique Constraints:**
- `users.email` (UNIQUE)
- `qr_codes.code_data` (UNIQUE)
- `event_staff(event_id, user_id)` (composite unique)

**Recommended Indexes:**
```sql
CREATE INDEX idx_events_organizer ON events(organizer_id);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_tickets_order ON tickets(order_id);
CREATE INDEX idx_ticket_orders_buyer ON ticket_orders(buyer_id);
CREATE INDEX idx_qr_codes_data ON qr_codes(code_data);
```

### 6.6 Transaction Management

Transactions are managed at the service layer using Spring's `@Transactional`:

```java
@Service
public class TicketServiceImpl implements TicketService {
    
    @Transactional
    public TicketOrderResponse purchaseTickets(PurchaseTicketRequest request) {
        // All operations within are atomic
        // Rollback on any RuntimeException
    }
}
```

**Transaction Rules:**
- Service methods modifying data use `@Transactional`
- Read-only operations use `@Transactional(readOnly = true)`
- Transactions propagate from service to repository layer
- RuntimeExceptions trigger automatic rollback

### 6.7 Migration Strategy (Flyway)

**Location:** `src/main/resources/db/migration/`

**Naming Convention:** `V{version}__{description}.sql`
- `V1__Create_enum_types.sql`
- `V2__Create_initial_tables.sql`
- `V3__Add_indexes.sql`

**Best Practices:**
- Never modify existing migration files
- Create new migrations for schema changes
- Test migrations against production-like data
- Use `spring.flyway.baseline-on-migrate=true` for existing databases

---

## 7. API Documentation

### 7.1 Base URL

```
http://localhost:8080/api
```

### 7.2 Authentication Endpoints

#### POST /api/auth/signup
**Description:** Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "ATTENDEE"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "ATTENDEE"
}
```

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 400 | Invalid input (validation errors) |
| 409 | Email already exists |

---

#### POST /api/auth/login
**Description:** Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "user@example.com",
  "role": "ATTENDEE"
}
```

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 401 | Invalid credentials |

---

### 7.3 Event Management Endpoints

#### GET /api/events
**Description:** Get all events for the authenticated organizer.

**Authorization:** `ORGANIZER` role required

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Summer Music Festival",
    "description": "Annual outdoor music event",
    "location": "Central Park",
    "startTime": "2026-07-15T18:00:00",
    "endTime": "2026-07-15T23:00:00",
    "status": "PUBLISHED",
    "organizerId": 1,
    "ticketTypes": [
      {
        "id": 1,
        "name": "General Admission",
        "price": 50.00,
        "totalQuantity": 1000,
        "soldCount": 150
      }
    ]
  }
]
```

---

#### POST /api/events
**Description:** Create a new event.

**Authorization:** `ORGANIZER` role required

**Request Body:**
```json
{
  "organizerId": 1,
  "name": "Summer Music Festival",
  "description": "Annual outdoor music event",
  "location": "Central Park",
  "startTime": "2026-07-15T18:00:00",
  "endTime": "2026-07-15T23:00:00"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Summer Music Festival",
  "status": "DRAFT",
  ...
}
```

**Validation Rules:**
- `name` is required (1-255 characters)
- `startTime` must be in the future
- `endTime` must be after `startTime`
- Event duration minimum: 30 minutes
- Event duration maximum: 14 days

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 400 | Validation errors |
| 404 | Organizer not found |

---

#### GET /api/events/{eventId}
**Description:** Get event details by ID.

**Authorization:** `ORGANIZER` role (must own the event)

**Response (200 OK):** Event object with ticket types

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 404 | Event not found |
| 403 | Not authorized to view |

---

#### PUT /api/events/{eventId}
**Description:** Update event details.

**Authorization:** `ORGANIZER` role (must own the event)

**Request Body:**
```json
{
  "name": "Updated Festival Name",
  "description": "Updated description",
  "location": "New Location",
  "startTime": "2026-07-20T18:00:00",
  "endTime": "2026-07-20T23:00:00",
  "status": "PUBLISHED"
}
```

**Status Transition Rules:**
- `DRAFT` → `PUBLISHED` (allowed)
- `PUBLISHED` → `CANCELLED` (allowed)
- `PUBLISHED` → `DRAFT` (allowed)
- Other transitions are invalid

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 400 | Invalid status transition or validation errors |
| 404 | Event not found |

---

#### DELETE /api/events/{eventId}
**Description:** Delete an event.

**Authorization:** `ORGANIZER` role (must own the event)

**Response (204 No Content)**

**Constraints:**
- Cannot delete events with sold tickets

---

### 7.4 Ticket Type Endpoints

#### POST /api/events/{eventId}/ticket-types
**Description:** Add a ticket type to an event.

**Authorization:** `ORGANIZER` role

**Request Body:**
```json
{
  "name": "VIP Access",
  "price": 150.00,
  "totalQuantity": 100
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "VIP Access",
  "price": 150.00,
  "totalQuantity": 100,
  "soldCount": 0
}
```

---

#### PUT /api/events/{eventId}/ticket-types/{ticketTypeId}
**Description:** Update a ticket type.

**Authorization:** `ORGANIZER` role

**Constraints:**
- Cannot reduce `totalQuantity` below `soldCount`

---

#### DELETE /api/events/{eventId}/ticket-types/{ticketTypeId}
**Description:** Remove a ticket type.

**Authorization:** `ORGANIZER` role

**Constraints:**
- Cannot delete if tickets have been sold

---

### 7.5 Staff Management Endpoints

#### GET /api/events/{eventId}/staff
**Description:** Get all staff assigned to an event.

**Authorization:** `ORGANIZER` role

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "eventId": 1,
    "userId": 5,
    "userEmail": "staff@example.com",
    "userName": "Jane Smith",
    "assignedAt": "2026-01-10T10:00:00"
  }
]
```

---

#### POST /api/events/{eventId}/staff
**Description:** Assign a staff member to an event.

**Authorization:** `ORGANIZER` role

**Request Body:**
```json
{
  "userId": 5
}
```

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 400 | User is already assigned |
| 404 | User or event not found |

---

#### DELETE /api/events/{eventId}/staff/{staffId}
**Description:** Remove staff assignment.

**Authorization:** `ORGANIZER` role

---

#### GET /api/events/staff/assigned
**Description:** Get events assigned to the authenticated staff member.

**Authorization:** `STAFF` role

**Response (200 OK):**
```json
{
  "staffId": 5,
  "staffEmail": "staff@example.com",
  "assignedEvents": [
    {
      "id": 1,
      "name": "Summer Music Festival",
      "location": "Central Park",
      "startTime": "2026-07-15T18:00:00"
    }
  ]
}
```

---

### 7.6 Public Event Endpoints

#### GET /api/public/events
**Description:** Browse all published events.

**Authorization:** None (public)

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | int | Page number (0-based) |
| `size` | int | Page size (default 20) |
| `sort` | string | Sort field (e.g., `startTime,asc`) |

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Summer Music Festival",
      "description": "Annual outdoor music event",
      "location": "Central Park",
      "startTime": "2026-07-15T18:00:00",
      "endTime": "2026-07-15T23:00:00",
      "ticketTypes": [...]
    }
  ],
  "pageable": {...},
  "totalElements": 50,
  "totalPages": 3
}
```

---

#### GET /api/public/events/{eventId}
**Description:** Get published event details.

**Authorization:** None (public)

**Constraints:**
- Only returns events with `PUBLISHED` status

---

### 7.7 Ticket Purchase Endpoints

#### POST /api/tickets/purchase
**Description:** Purchase tickets for an event.

**Authorization:** `ATTENDEE` role

**Request Body:**
```json
{
  "eventId": 1,
  "buyerEmail": "buyer@example.com",
  "buyerFirstName": "John",
  "buyerLastName": "Doe",
  "items": [
    {
      "ticketTypeId": 1,
      "quantity": 2
    },
    {
      "ticketTypeId": 2,
      "quantity": 1
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "orderId": 1,
  "eventId": 1,
  "eventName": "Summer Music Festival",
  "buyerEmail": "buyer@example.com",
  "status": "CONFIRMED",
  "tickets": [
    {
      "ticketId": 1,
      "ticketTypeName": "General Admission",
      "qrCodeData": "TKT-abc123..."
    },
    {
      "ticketId": 2,
      "ticketTypeName": "General Admission",
      "qrCodeData": "TKT-def456..."
    }
  ],
  "totalAmount": 250.00,
  "createdAt": "2026-01-15T10:30:00"
}
```

**Validation:**
- Event must be `PUBLISHED`
- Sufficient ticket inventory required
- All ticket types must belong to the event

**Error Cases:**
| Status | Condition |
|--------|-----------|
| 400 | Validation errors, insufficient inventory |
| 404 | Event or ticket type not found |

---

#### GET /api/tickets
**Description:** Get all tickets for the authenticated user.

**Authorization:** `ATTENDEE` role

**Response (200 OK):** Array of ticket objects

---

#### GET /api/tickets/{ticketId}
**Description:** Get ticket details.

**Authorization:** `ATTENDEE` role (must own the ticket)

---

#### GET /api/tickets/{ticketId}/qr-code
**Description:** Get QR code image for a ticket.

**Authorization:** `ATTENDEE` role (must own the ticket)

**Response (200 OK):**
- Content-Type: `image/png`
- Body: PNG image binary

---

#### GET /api/tickets/{ticketId}/download
**Description:** Download ticket as PDF.

**Authorization:** `ATTENDEE` role (must own the ticket)

**Response (200 OK):**
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="ticket-{id}.pdf"`

---

### 7.8 Ticket Validation Endpoints

#### POST /api/validation/validate
**Description:** Validate a ticket for event entry.

**Authorization:** `STAFF` role (must be assigned to the event)

**Request Body:**
```json
{
  "qrCodeData": "TKT-abc123...",
  "eventId": 1,
  "method": "QR_CODE",
  "notes": "Optional notes"
}
```

**Response (200 OK):**
```json
{
  "ticketId": 1,
  "status": "VALID",
  "message": "Ticket validated successfully",
  "ticketHolderName": "John Doe",
  "ticketType": "General Admission",
  "validatedAt": "2026-07-15T18:30:00"
}
```

**Validation Status Values:**
| Status | Meaning |
|--------|---------|
| `VALID` | Ticket is valid and marked as used |
| `INVALID` | Ticket not found or doesn't belong to event |
| `ALREADY_USED` | Ticket has already been used |

---

#### GET /api/validation/events/{eventId}/attempts
**Description:** Get validation attempt history for an event.

**Authorization:** `STAFF` role (must be assigned to the event)

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | int | Page number |
| `size` | int | Page size |
| `status` | string | Filter by status |

---

### 7.9 Analytics Endpoints

#### GET /api/analytics/events/{eventId}/sales
**Description:** Get sales analytics for an event.

**Authorization:** `ORGANIZER` role (must own the event)

**Response (200 OK):**
```json
{
  "eventId": 1,
  "eventName": "Summer Music Festival",
  "totalTicketsSold": 500,
  "totalRevenue": 35000.00,
  "salesByTicketType": [
    {
      "ticketTypeId": 1,
      "ticketTypeName": "General Admission",
      "sold": 400,
      "revenue": 20000.00
    },
    {
      "ticketTypeId": 2,
      "ticketTypeName": "VIP",
      "sold": 100,
      "revenue": 15000.00
    }
  ]
}
```

---

#### GET /api/analytics/events/{eventId}/orders
**Description:** Get order statistics for an event.

**Authorization:** `ORGANIZER` role (must own the event)

**Response (200 OK):**
```json
{
  "eventId": 1,
  "totalOrders": 250,
  "ordersByStatus": {
    "CONFIRMED": 240,
    "CANCELLED": 8,
    "REFUNDED": 2
  },
  "averageOrderValue": 140.00
}
```

---

### 7.10 Pagination, Filtering, and Sorting

**Standard Pagination Parameters:**
```
GET /api/resource?page=0&size=20&sort=createdAt,desc
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | 0 | Zero-based page index |
| `size` | 20 | Number of items per page |
| `sort` | varies | Field and direction (`field,asc` or `field,desc`) |

**Paginated Response Structure:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {...}
  },
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## 8. Business Logic Flow

### 8.1 Event Creation Flow

```
1. [Client] POST /api/events with event data
        │
        ▼
2. [EventController] Receives request, validates authorization
        │
        ▼
3. [EventServiceImpl.createEvent()]
   ├── Fetch organizer from UserRepository
   ├── Map request to Event entity
   ├── Validate event properties:
   │   ├── Name not empty
   │   ├── Start time in future
   │   ├── End time after start time
   │   ├── Duration within limits
   │   └── Location not empty
   ├── Set initial status to DRAFT
   └── Save via EventRepository
        │
        ▼
4. [EventMapper] Convert saved entity to EventResponse
        │
        ▼
5. [Controller] Return 201 Created with response body
```

### 8.2 Ticket Purchase Flow

```
1. [Client] POST /api/tickets/purchase
        │
        ▼
2. [TicketController] Authorization check (ATTENDEE role)
        │
        ▼
3. [TicketServiceImpl.purchaseTickets()] @Transactional
   │
   ├── 3a. Validate event exists and is PUBLISHED
   │
   ├── 3b. For each item in request:
   │       ├── Fetch TicketType
   │       ├── Check inventory (quantity <= available)
   │       └── Accumulate totals
   │
   ├── 3c. Create TicketOrder entity
   │       └── Save to TicketOrderRepository
   │
   ├── 3d. For each ticket:
   │       ├── Create Ticket entity
   │       ├── Generate unique QR code data
   │       ├── Create QrCode entity
   │       ├── Link Ticket to QrCode
   │       ├── Save Ticket
   │       └── Increment TicketType.soldCount
   │
   └── 3e. Return TicketOrderResponse with all tickets
        │
        ▼
4. [Controller] Return 201 Created
```

**Atomicity:** The entire purchase is wrapped in `@Transactional`. If any step fails (e.g., insufficient inventory), the entire transaction rolls back.

### 8.3 Ticket Validation Flow

```
1. [Staff] POST /api/validation/validate with QR code data
        │
        ▼
2. [TicketValidationController] Authorization check (STAFF role)
        │
        ▼
3. [TicketValidationServiceImpl.validateTicket()] @Transactional
   │
   ├── 3a. Verify staff is assigned to event
   │
   ├── 3b. Find ticket by QR code data
   │       └── If not found: return INVALID status
   │
   ├── 3c. Verify ticket belongs to the event
   │       └── If mismatch: return INVALID status
   │
   ├── 3d. Check ticket status
   │       └── If already USED: return ALREADY_USED status
   │
   ├── 3e. Mark ticket as USED
   │       └── Update QrCode status to USED
   │
   ├── 3f. Create TicketValidationAttempt record
   │       └── Save for audit trail
   │
   └── 3g. Return validation result
        │
        ▼
4. [Controller] Return 200 OK with validation status
```

### 8.4 Event Status Transition Flow

```
DRAFT ─────────────────────► PUBLISHED
  │                              │
  │                              ▼
  │                         CANCELLED
  │                              │
  │                              │
  └──────────────────────────────┘
           (can revert to DRAFT)

Valid Transitions:
├── DRAFT → PUBLISHED (event goes live)
├── PUBLISHED → CANCELLED (event cancelled)
└── PUBLISHED → DRAFT (unpublish for edits)

Invalid Transitions (throw EventValidationException):
├── DRAFT → CANCELLED (must publish first)
├── CANCELLED → PUBLISHED (cannot reactivate)
└── CANCELLED → DRAFT (cannot reactivate)
```

### 8.5 Validation Strategy

**Request Validation (Controller Layer):**
- Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Email`)
- Validated automatically by Spring

**Business Validation (Service Layer):**
- Custom validation methods (e.g., `validateNewEvent()`)
- Cross-field validation
- State-dependent validation
- Throws custom exceptions

**Example Business Validation:**
```java
private void validateNewEvent(Event event) {
    if (event.getName() == null || event.getName().isBlank()) {
        throw new EventValidationException("Event name is required.");
    }
    if (event.getStartTime() == null || event.getStartTime().isBefore(LocalDateTime.now())) {
        throw new EventValidationException("Event start time must be in the future.");
    }
    // ... more validations
}
```

### 8.6 Error Handling Flow

```
1. Exception thrown in Service layer
        │
        ▼
2. Exception propagates to Controller
        │
        ▼
3. GlobalExceptionHandler intercepts
   ├── Maps exception to HTTP status
   ├── Constructs error response DTO
   └── Returns standardized error body
        │
        ▼
4. Client receives error response:
   {
     "status": 400,
     "error": "Bad Request",
     "message": "Event start time must be in the future.",
     "timestamp": "2026-01-15T10:30:00"
   }
```

---

## 9. Security

### 9.1 Authentication Method

**Type:** JWT (JSON Web Token) - Stateless Authentication

**Flow:**
```
1. User sends credentials to /api/auth/login
2. Server validates credentials against database
3. Server generates JWT with user claims
4. Client stores JWT (localStorage/cookie)
5. Client sends JWT in Authorization header for subsequent requests
6. Server validates JWT signature and expiration
7. Server extracts user context from JWT claims
```

### 9.2 JWT Structure

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "user@example.com",
  "userId": 1,
  "role": "ORGANIZER",
  "iat": 1705312800,
  "exp": 1705399200
}
```

**Configuration:**
- Algorithm: HS256 (HMAC-SHA256)
- Expiration: 24 hours (configurable via `jwt.expiration`)
- Secret: Configured via `jwt.secret` environment variable

### 9.3 Authorization Strategy

**Role-Based Access Control (RBAC):**

| Role | Permissions |
|------|-------------|
| `ORGANIZER` | Create/manage own events, assign staff, view analytics |
| `STAFF` | Validate tickets for assigned events |
| `ATTENDEE` | Browse events, purchase tickets, view own tickets |

**Authorization Annotations:**
```java
@PreAuthorize("hasRole('ORGANIZER')")
public ResponseEntity<EventResponse> createEvent(...) { ... }

@PreAuthorize("hasRole('ORGANIZER')")
public ResponseEntity<List<EventResponse>> getEvents(...) { ... }
```

### 9.4 Security Filter Chain

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, 
            UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

**Filter Order:**
1. CORS Filter
2. JWT Authentication Filter (custom)
3. Authorization Filter
4. Controller

### 9.5 Public Endpoints

The following endpoints are accessible without authentication:

| Endpoint | Purpose |
|----------|---------|
| `POST /api/auth/signup` | User registration |
| `POST /api/auth/login` | User authentication |
| `GET /api/public/events` | Browse published events |
| `GET /api/public/events/{id}` | View event details |
| `GET /swagger-ui/**` | API documentation |
| `GET /v3/api-docs/**` | OpenAPI spec |

### 9.6 Security Best Practices Implemented

| Practice | Implementation |
|----------|----------------|
| Password Hashing | BCrypt with strength 10 |
| Stateless Sessions | No server-side session storage |
| CSRF Protection | Disabled (stateless JWT, not needed) |
| Input Validation | Bean Validation + custom validation |
| SQL Injection Prevention | Parameterized queries via JPA |
| Resource Authorization | Owner checks in service layer |

### 9.7 Common Security Pitfalls Avoided

| Pitfall | Mitigation |
|---------|------------|
| Token stored in JWT | Only user ID and role stored, not sensitive data |
| No token expiration | 24-hour expiration enforced |
| Weak secrets | JWT secret must be 256+ bits (environment variable) |
| Missing ownership checks | Service layer verifies resource ownership |
| IDOR vulnerabilities | All resource access validated against authenticated user |

---

## 10. Error Handling & Logging

### 10.1 Global Exception Handling

The application uses `@RestControllerAdvice` for centralized exception handling.

**Location:** `com.project.event_ticket_platform.exceptions.GlobalExceptionHandler`

**Structure:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventNotFound(EventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }
    
    @ExceptionHandler(EventValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(EventValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }
    
    // ... other handlers
}
```

### 10.2 Custom Exceptions

| Exception | HTTP Status | Usage |
|-----------|-------------|-------|
| `EventNotFoundException` | 404 | Event ID not found |
| `TicketNotFoundException` | 404 | Ticket ID not found |
| `OrganizerNotFoundException` | 404 | Organizer ID not found |
| `TicketTypeNotFoundException` | 404 | Ticket type ID not found |
| `EventValidationException` | 400 | Business rule violation |
| `InsufficientTicketInventoryException` | 400 | Not enough tickets available |
| `UnauthorizedAccessException` | 403 | User lacks permission |
| `DuplicateEmailException` | 409 | Email already registered |

### 10.3 Error Response Format

All errors return a consistent JSON structure:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Event start time must be in the future.",
  "timestamp": "2026-01-15T10:30:00.000Z",
  "path": "/api/events"
}
```

### 10.4 Logging Strategy

**Logging Framework:** SLF4J with Logback (Spring Boot default)

**Log Levels:**
| Level | Usage |
|-------|-------|
| `ERROR` | Exceptions, failures requiring attention |
| `WARN` | Recoverable issues, deprecation warnings |
| `INFO` | Significant business events (startup, shutdown, major operations) |
| `DEBUG` | Detailed flow information for debugging |
| `TRACE` | Very detailed debugging (rarely used) |

**Recommended Logging Patterns:**

```java
@Service
@Slf4j
public class EventServiceImpl implements EventService {
    
    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event for organizer: {}", request.organizerId());
        
        try {
            // ... business logic
            log.debug("Event created with ID: {}", savedEvent.getId());
            return response;
        } catch (Exception e) {
            log.error("Failed to create event: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### 10.5 Configuration

**application.properties:**
```properties
# Logging levels
logging.level.root=INFO
logging.level.com.project.event_ticket_platform=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Log format
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### 10.6 Monitoring Considerations

**Recommended Additions for Production:**
- Spring Boot Actuator for health checks and metrics
- Structured logging (JSON format) for log aggregation
- Correlation IDs for request tracing
- Metrics export to Prometheus/Datadog

---

## 11. Testing Strategy

### 11.1 Testing Pyramid

```
        /\
       /  \      E2E Tests (few)
      /────\
     /      \    Integration Tests (moderate)
    /────────\
   /          \  Unit Tests (many)
  /────────────\
```

### 11.2 Unit Tests

**What to Test:**
- Service layer business logic
- Validation methods
- Mappers (transformation logic)
- Utility classes
- Edge cases and error conditions

**What NOT to Test:**
- Simple getters/setters (Lombok generates these)
- Spring framework code
- Third-party libraries
- Trivial methods with no logic

**Example Unit Test:**
```java
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    
    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EventMapper eventMapper;
    
    @InjectMocks
    private EventServiceImpl eventService;
    
    @Test
    void createEvent_WithValidData_ReturnsEventResponse() {
        // Arrange
        CreateEventRequest request = new CreateEventRequest(...);
        User organizer = new User();
        Event event = new Event();
        EventResponse expectedResponse = new EventResponse(...);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(eventMapper.toEntity(request)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(expectedResponse);
        
        // Act
        EventResponse result = eventService.createEvent(request);
        
        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(eventRepository).save(event);
    }
    
    @Test
    void createEvent_WithNonExistentOrganizer_ThrowsException() {
        // Arrange
        CreateEventRequest request = new CreateEventRequest(999L, ...);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(OrganizerNotFoundException.class, 
            () -> eventService.createEvent(request));
    }
}
```

### 11.3 Integration Tests

**Purpose:** Test component interaction with real database and Spring context.

**Configuration:**
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Test
    void getEvents_ReturnsEventList() throws Exception {
        mockMvc.perform(get("/api/events")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

### 11.4 Mocking Strategy

| Component | Mocking Approach |
|-----------|------------------|
| Repositories | `@Mock` with Mockito |
| External Services | `@Mock` or WireMock |
| Security Context | `@WithMockUser` or custom `SecurityContext` |
| Time-dependent code | `Clock` injection |

### 11.5 Test Naming Conventions

**Format:** `methodName_StateUnderTest_ExpectedBehavior`

**Examples:**
- `createEvent_WithValidData_ReturnsEventResponse`
- `createEvent_WithPastStartTime_ThrowsValidationException`
- `validateTicket_WithAlreadyUsedTicket_ReturnsAlreadyUsedStatus`

### 11.6 Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=EventServiceImplTest

# Run with coverage report
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dgroups=integration

# Skip tests during build
./mvnw package -DskipTests
```

### 11.7 Test Data Management

**For Unit Tests:** Create test objects in test methods or use test fixtures.

**For Integration Tests:**
- Use `@Sql` to load test data
- Use `@DirtiesContext` for isolation
- Clean up in `@AfterEach` methods

---

## 12. Javadoc Requirements

### 12.1 Controller Javadoc Example

```java
/**
 * REST controller for managing events.
 * 
 * <p>Provides endpoints for event CRUD operations, accessible to users
 * with the ORGANIZER role. All endpoints require JWT authentication.</p>
 * 
 * @see EventService
 * @see EventResponse
 */
@RestController
@RequestMapping("/api/events")
public class EventController {
    
    /**
     * Creates a new event.
     * 
     * <p>The event is created in DRAFT status. The authenticated user must
     * have the ORGANIZER role and must be the organizer specified in the request.</p>
     * 
     * @param request the event creation request containing event details
     * @return ResponseEntity containing the created event with HTTP 201 status
     * @throws OrganizerNotFoundException if the specified organizer does not exist
     * @throws EventValidationException if the event data fails validation
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        // ...
    }
}
```

### 12.2 Service Javadoc Example

```java
/**
 * Service interface for event management operations.
 * 
 * <p>Defines the contract for event-related business logic including
 * creation, retrieval, update, and deletion of events.</p>
 */
public interface EventService {
    
    /**
     * Creates a new event for the specified organizer.
     * 
     * <p>Validates all event properties including:
     * <ul>
     *   <li>Event name is not empty</li>
     *   <li>Start time is in the future</li>
     *   <li>End time is after start time</li>
     *   <li>Duration is within allowed limits</li>
     * </ul>
     * </p>
     * 
     * @param request the event creation request
     * @return the created event response
     * @throws OrganizerNotFoundException if organizer ID is invalid
     * @throws EventValidationException if validation fails
     */
    EventResponse createEvent(CreateEventRequest request);
}
```

### 12.3 Repository Javadoc Example

```java
/**
 * Repository interface for Event entity persistence operations.
 * 
 * <p>Extends Spring Data JPA repository with custom query methods
 * for event-specific data access patterns.</p>
 */
public interface EventRepository extends JpaRepository<Event, Long> {
    
    /**
     * Finds all events organized by a specific user.
     * 
     * @param organizerId the ID of the organizer
     * @return list of events for the organizer, empty list if none found
     */
    List<Event> findByOrganizerId(Long organizerId);
    
    /**
     * Finds all published events ordered by start time.
     * 
     * @param status the event status to filter by
     * @param pageable pagination parameters
     * @return page of events matching the status
     */
    Page<Event> findByStatusOrderByStartTimeAsc(EventStatus status, Pageable pageable);
}
```

### 12.4 Javadoc Standards

**Required Elements:**
- Class-level description explaining purpose
- Method description (what it does, not how)
- `@param` for each parameter
- `@return` describing return value
- `@throws` for each exception

**Style Guidelines:**
- Use complete sentences
- Start with a verb (Creates, Returns, Validates)
- Document business rules and constraints
- Reference related classes with `@see`

---

## 13. How to Run the Project

### 13.1 Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java JDK | 21+ | Temurin or Oracle |
| Maven | 3.9+ | Or use included `mvnw` wrapper |
| Neon DB Account | - | Serverless PostgreSQL database (sign up at neon.tech) |
| Git | 2.x | For version control |

### 13.2 Local Setup Steps

**1. Clone the Repository:**
```bash
git clone https://github.com/your-org/event-ticket-platform-backend.git
cd event-ticket-platform-backend
```

**2. Set Up Neon DB:**
- Create an account at [neon.tech](https://neon.tech)
- Create a new project and database
- Copy your connection string from the Neon dashboard
- The connection string format: `postgresql://user:password@host/database?sslmode=require`

**3. Configure Environment Variables:**
```bash
# Linux/macOS
export DATABASE_URL=postgresql://user:password@host.neon.tech/database?sslmode=require
export JWT_SECRET=your-256-bit-secret-key-here-must-be-long-enough

# Windows PowerShell
$env:DATABASE_URL="postgresql://user:password@host.neon.tech/database?sslmode=require"
$env:JWT_SECRET="your-256-bit-secret-key-here-must-be-long-enough"
```

**4. Update application.properties (if needed):**
If using `DATABASE_URL` environment variable, Spring Boot will automatically parse it. Alternatively, configure manually:
```properties
spring.datasource.url=${DATABASE_URL}
# Or manually:
# spring.datasource.url=jdbc:postgresql://host.neon.tech/database?sslmode=require
```

### 13.3 Build Commands

```bash
# Clean and build
./mvnw clean package

# Build without tests
./mvnw clean package -DskipTests

# Build with specific profile
./mvnw clean package -Pproduction
```

### 13.4 Run Commands

```bash
# Run with Maven
./mvnw spring-boot:run

# Run JAR directly
java -jar target/event-ticket-platform-0.0.1-SNAPSHOT.jar

# Run with specific profile
java -jar target/event-ticket-platform-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Run with environment variables
DATABASE_URL=postgresql://user:password@host.neon.tech/database?sslmode=require JWT_SECRET=mysecret java -jar target/event-ticket-platform-0.0.1-SNAPSHOT.jar
```

### 13.5 Verify Installation

**1. Check Application Status:**
```bash
curl http://localhost:8080/actuator/health
```

**2. Access Swagger UI:**
Open browser to: `http://localhost:8080/swagger-ui.html`

**3. Test Authentication:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","firstName":"Test","lastName":"User","role":"ATTENDEE"}'
```

### 13.6 Common Startup Issues and Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| `Connection refused` | Invalid Neon DB connection string | Verify DATABASE_URL from Neon dashboard |
| `Access denied for user` | Wrong credentials | Check DATABASE_URL contains correct user and password |
| `Flyway migration failed` | Schema conflict | Check Neon DB branch is correct, or use `spring.flyway.clean-on-validation-error=true` |
| `JWT secret too short` | Secret < 256 bits | Use a longer secret (32+ characters) |
| `Port already in use` | Another service on 8080 | Change port or stop conflicting service |
| `Bean creation exception` | Missing dependency | Check Maven dependencies, run `./mvnw dependency:resolve` |

---

## 14. Deployment

### 14.1 Deployment Strategy

**Recommended:** Containerized deployment using Docker with orchestration (Kubernetes or Docker Compose).

**Deployment Environments:**
| Environment | Purpose | Database |
|-------------|---------|----------|
| `dev` | Local development | Neon DB (development branch) |
| `staging` | Pre-production testing | Neon DB (staging branch) |
| `prod` | Production | Neon DB (production branch) |

### 14.2 Environment Variables

**Required for Production:**
| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | Neon DB connection string | `postgresql://user:password@host.neon.tech/db?sslmode=require` |
| `JWT_SECRET` | JWT signing key (256+ bits) | `base64-encoded-256-bit-key` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |

**Optional:**
| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | `8080` |
| `JWT_EXPIRATION` | Token validity (ms) | `86400000` |

### 14.3 Docker Configuration

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/event-ticket-platform-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and Run:**
```bash
# Build image
docker build -t event-ticket-platform:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=postgresql://user:password@host.neon.tech/database?sslmode=require \
  -e JWT_SECRET=your-secret-key \
  -e SPRING_PROFILES_ACTIVE=prod \
  event-ticket-platform:latest
```

**Docker Compose (using Neon DB):**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=${DATABASE_URL}
      - JWT_SECRET=${JWT_SECRET}
      - SPRING_PROFILES_ACTIVE=prod
```

**Alternative: Docker Compose with local PostgreSQL (for development only):**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/event_ticket_platform
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - db
  
  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=event_ticket_platform
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### 14.4 CI/CD Pipeline

**GitHub Actions Workflow (`.github/workflows/ci.yml`):**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Build with Maven
        run: ./mvnw clean verify
      
      - name: Build Docker image
        run: docker build -t event-ticket-platform:${{ github.sha }} .
```

### 14.5 Production Checklist

- [ ] Environment variables configured
- [ ] Database migrations tested
- [ ] JWT secret is strong and unique
- [ ] CORS origins restricted to known domains
- [ ] Logging configured for production (INFO level, structured)
- [ ] Health checks configured
- [ ] Monitoring and alerting set up
- [ ] Backup strategy for database
- [ ] SSL/TLS configured (via reverse proxy)

---

## 15. Contribution Guidelines

### 15.1 Coding Standards

**Java Style:**
- Follow Google Java Style Guide
- Use 4 spaces for indentation (not tabs)
- Maximum line length: 120 characters
- Use meaningful variable and method names

**Spring Conventions:**
- Use constructor injection (not field injection)
- Prefer interfaces for services
- Use `@Transactional` at service layer
- Keep controllers thin (delegate to services)

**DTO Conventions:**
- Use Java records for immutable DTOs
- Name requests as `*Request`, responses as `*Response`
- Include validation annotations on request DTOs

### 15.2 Git Workflow

**Branching Model:** GitFlow-inspired

```
main (production)
├── develop (integration)
│   ├── feature/add-payment-gateway
│   ├── feature/improve-validation
│   └── bugfix/fix-ticket-count
└── release/v1.2.0
    └── hotfix/security-patch
```

**Branch Types:**
| Type | Purpose | Base | Merge To |
|------|---------|------|----------|
| `feature/*` | New features | `develop` | `develop` |
| `bugfix/*` | Bug fixes | `develop` | `develop` |
| `release/*` | Release preparation | `develop` | `main`, `develop` |
| `hotfix/*` | Production fixes | `main` | `main`, `develop` |

### 15.3 Branching Strategy

**Creating a Feature Branch:**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

**Completing a Feature:**
```bash
git checkout develop
git pull origin develop
git merge feature/your-feature-name
git push origin develop
```

### 15.4 Commit Message Format

**Format:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no code change |
| `refactor` | Code restructuring |
| `test` | Adding tests |
| `chore` | Maintenance tasks |

**Examples:**
```
feat(events): add event cancellation endpoint

Adds DELETE /api/events/{id}/cancel endpoint that allows organizers
to cancel published events. Sends notification to ticket holders.

Closes #123
```

```
fix(tickets): correct inventory check on purchase

Previously, concurrent purchases could oversell tickets. Added
pessimistic locking to prevent race conditions.

Fixes #456
```

### 15.5 Pull Request Process

1. **Create PR** from feature branch to `develop`
2. **Fill PR template** with description and test plan
3. **Ensure CI passes** (all tests green)
4. **Request review** from at least one team member
5. **Address feedback** and update PR
6. **Squash and merge** when approved

**PR Template:**
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings
```

### 15.6 Adding New Features Safely

1. **Create feature branch** from latest `develop`
2. **Write tests first** (TDD approach recommended)
3. **Implement feature** following existing patterns
4. **Update documentation** if API changes
5. **Run full test suite** locally
6. **Create PR** with detailed description
7. **Address review feedback**
8. **Merge only when** CI passes and approved

---

## 16. Extension Guide

### 16.1 Adding a New Endpoint

**Example:** Adding a `GET /api/events/{id}/attendees` endpoint

**Step 1: Define the Response DTO**
```java
// src/main/java/com/project/event_ticket_platform/dtos/AttendeeResponse.java
public record AttendeeResponse(
    Long ticketId,
    String attendeeName,
    String attendeeEmail,
    String ticketType,
    LocalDateTime purchaseDate
) {}
```

**Step 2: Add Repository Method (if needed)**
```java
// In TicketRepository.java
@Query("SELECT t FROM Ticket t WHERE t.order.event.id = :eventId")
List<Ticket> findByEventId(@Param("eventId") Long eventId);
```

**Step 3: Add Service Method**
```java
// In EventService.java
List<AttendeeResponse> getEventAttendees(Long eventId);

// In EventServiceImpl.java
@Override
@Transactional(readOnly = true)
public List<AttendeeResponse> getEventAttendees(Long eventId) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new EventNotFoundException(eventId));
    
    return ticketRepository.findByEventId(eventId).stream()
        .map(ticket -> new AttendeeResponse(
            ticket.getId(),
            ticket.getOrder().getBuyerFirstName() + " " + ticket.getOrder().getBuyerLastName(),
            ticket.getOrder().getBuyerEmail(),
            ticket.getTicketType().getName(),
            ticket.getCreatedAt()
        ))
        .toList();
}
```

**Step 4: Add Controller Method**
```java
// In EventController.java
@GetMapping("/{eventId}/attendees")
@PreAuthorize("hasRole('ORGANIZER')")
public ResponseEntity<List<AttendeeResponse>> getEventAttendees(
        @PathVariable Long eventId) {
    return ResponseEntity.ok(eventService.getEventAttendees(eventId));
}
```

**Step 5: Write Tests**
```java
@Test
void getEventAttendees_ReturnsAttendeeList() {
    // Arrange
    // Act
    // Assert
}
```

### 16.2 Adding a New Entity

**Example:** Adding a `Venue` entity

**Step 1: Create Entity Class**
```java
// src/main/java/com/project/event_ticket_platform/entities/Venue.java
@Entity
@Table(name = "venues")
@Getter @Setter
@NoArgsConstructor
public class Venue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String address;
    
    private Integer capacity;
    
    @OneToMany(mappedBy = "venue")
    private List<Event> events = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Step 2: Create Flyway Migration**
```sql
-- src/main/resources/db/migration/V3__Create_venues_table.sql
CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    capacity INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE events ADD COLUMN venue_id BIGINT REFERENCES venues(id);
```

**Step 3: Create Repository**
```java
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByNameContainingIgnoreCase(String name);
}
```

**Step 4: Create DTOs**
```java
public record CreateVenueRequest(
    @NotBlank String name,
    @NotBlank String address,
    @Positive Integer capacity
) {}

public record VenueResponse(
    Long id,
    String name,
    String address,
    Integer capacity
) {}
```

**Step 5: Create Mapper**
```java
@Mapper(componentModel = "spring")
public interface VenueMapper {
    Venue toEntity(CreateVenueRequest request);
    VenueResponse toResponse(Venue venue);
}
```

**Step 6: Create Service and Controller** (follow existing patterns)

### 16.3 Modifying Business Logic

**Example:** Adding a maximum tickets per order limit

**Step 1: Identify the Service Method**
Location: `TicketServiceImpl.purchaseTickets()`

**Step 2: Add Configuration Property**
```properties
# application.properties
app.tickets.max-per-order=10
```

**Step 3: Inject Configuration**
```java
@Service
public class TicketServiceImpl implements TicketService {
    
    @Value("${app.tickets.max-per-order:10}")
    private int maxTicketsPerOrder;
    
    // ...
}
```

**Step 4: Add Validation Logic**
```java
@Override
@Transactional
public TicketOrderResponse purchaseTickets(PurchaseTicketRequest request) {
    int totalQuantity = request.items().stream()
        .mapToInt(PurchaseItem::quantity)
        .sum();
    
    if (totalQuantity > maxTicketsPerOrder) {
        throw new EventValidationException(
            "Cannot purchase more than " + maxTicketsPerOrder + " tickets per order");
    }
    
    // ... rest of the method
}
```

**Step 5: Update Tests**
```java
@Test
void purchaseTickets_ExceedsMaxLimit_ThrowsException() {
    // Test the new validation
}
```

### 16.4 Adding External Integration

**Example:** Integrating with a Payment Gateway (Stripe)

**Step 1: Add Dependency**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.0.0</version>
</dependency>
```

**Step 2: Create Configuration**
```java
@Configuration
public class StripeConfig {
    
    @Value("${stripe.api-key}")
    private String apiKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }
}
```

**Step 3: Create Integration Service**
```java
public interface PaymentService {
    PaymentResult processPayment(PaymentRequest request);
}

@Service
public class StripePaymentService implements PaymentService {
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            PaymentIntent intent = PaymentIntent.create(
                PaymentIntentCreateParams.builder()
                    .setAmount(request.amountInCents())
                    .setCurrency("usd")
                    .build()
            );
            return new PaymentResult(true, intent.getId(), null);
        } catch (StripeException e) {
            return new PaymentResult(false, null, e.getMessage());
        }
    }
}
```

**Step 4: Integrate with Existing Service**
```java
@Service
public class TicketServiceImpl implements TicketService {
    
    private final PaymentService paymentService;
    
    @Override
    @Transactional
    public TicketOrderResponse purchaseTickets(PurchaseTicketRequest request) {
        // ... validate and calculate total
        
        PaymentResult payment = paymentService.processPayment(
            new PaymentRequest(totalAmountCents, request.paymentToken())
        );
        
        if (!payment.success()) {
            throw new PaymentFailedException(payment.errorMessage());
        }
        
        // ... create order and tickets
    }
}
```

**Step 5: Add Environment Variable**
```properties
stripe.api-key=${STRIPE_API_KEY}
```

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| **Event** | A scheduled occurrence (concert, conference) with tickets for sale |
| **Organizer** | User who creates and manages events |
| **Staff** | User assigned to validate tickets at events |
| **Attendee** | User who purchases and uses tickets |
| **Ticket Type** | Category of tickets (GA, VIP) with specific pricing and inventory |
| **Ticket Order** | A purchase transaction containing one or more tickets |
| **QR Code** | Unique scannable code for ticket validation |
| **Validation Attempt** | Record of a ticket scan attempt at event entry |

## Appendix B: Quick Reference

### API Authentication Header
```
Authorization: Bearer <jwt_token>
```

### Common HTTP Status Codes
| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Server Error |

### Maven Commands
```bash
./mvnw clean install      # Build project
./mvnw test               # Run tests
./mvnw spring-boot:run    # Run application
./mvnw dependency:tree    # View dependencies
```

---

*This documentation was generated for the Event Ticket Platform Backend v1.0.0*
