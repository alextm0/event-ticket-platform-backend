# Project Structure — Role of Each Directory

Your layout follows a clear **layered architecture**. Here’s what each part is for.

---

## Source root

| Item | Role |
|------|------|
| **`Application.java`** | Spring Boot entry point. Runs the app and component scanning. |

---

## `config/`

**Role:** Application-wide configuration and infrastructure.

- **SecurityConfig** — HTTP security: which paths are public, JWT filter, CORS, session stateless.
- **JwtAuthenticationFilter** — Reads the `Authorization` header, validates JWT, puts the user (e.g. UUID) into `SecurityContext`.
- **JwtService** lives in `services/` but is used here; it’s the JWT utility (parse, validate, extract userId/role).
- **PasswordConfig** — BCrypt bean for hashing passwords.
- **OpenApiConfig** — Swagger/OpenAPI (paths, UI).
- **JpaAuditingConfig** — Enables `@CreatedDate` / `@LastModifiedDate` on entities.

So: **security, HTTP, and cross-cutting setup**, not business logic.

---

## `controllers/`

**Role:** HTTP boundary. Handle requests and responses only.

- Map URLs to methods (`@GetMapping`, `@PostMapping`, etc.).
- Validate request bodies (`@Valid`) and path/query params.
- Call **one or more services**; no business logic here.
- Return DTOs and HTTP status (often via `ResponseEntity`).

**`controllers/advice/`**

- **GlobalExceptionHandler** — One place that catches exceptions (your custom ones + validation + Spring Security `AccessDeniedException`, etc.) and turns them into **ProblemDetail** (RFC 7807) and the right status (400, 403, 404, 409, 500). Keeps error responses consistent.

So: **controllers = thin HTTP layer**; **advice = centralized error responses**.

---

## `dtos/`

**Role:** Data shapes at the API boundary. No behavior, only structure.

- **Request DTOs** (e.g. `CreateEventRequest`, `PurchaseTicketRequest`) — What the client sends; often with validation annotations (`@NotBlank`, `@Email`, etc.).
- **Response DTOs** (e.g. `EventResponse`, `TicketResponse`) — What the API returns. Decouples the internal model (entities) from the contract.

Benefits: stable API contract, validation in one place, no exposing entities (and their relations) directly.

---

## `entities/`

**Role:** Persistence model — what is stored in the database.

- JPA `@Entity` classes (e.g. `Event`, `Ticket`, `User`, `TicketOrder`).
- Enums used as entity fields or in the domain (e.g. `EventStatus`, `UserRole`, `TicketStatus`).

Used by **repositories** and **services** (and mapped to DTOs before leaving the app). Controllers should not depend on entities directly; they work with DTOs.

---

## `exceptions/`

**Role:** Domain/application exceptions.

- Custom exceptions (e.g. `EventNotFoundException`, `InsufficientTicketsException`, `UnauthorizedAccessException`) thrown by services when a rule is broken or something is missing.
- **GlobalExceptionHandler** (in `controllers/advice`) maps these to HTTP status and ProblemDetail.

So: **exceptions = “what went wrong”**; **advice = “how to respond over HTTP”**.

---

## `mappers/`

**Role:** Convert between **entities** and **DTOs**.

- MapStruct interfaces (e.g. `EventMapper`, `TicketMapper`) — `toEntity(request)`, `toResponse(entity)`.
- Keeps mapping logic in one place, type-safe, and easy to test. Controllers and services stay clean.

---

## `repositories/`

**Role:** Data access. “How to read/write the database.”

- Spring Data JPA interfaces (e.g. `EventRepository`, `TicketRepository`) — `findById`, `save`, and custom `@Query` methods.
- No business rules here; only queries and persistence. Used only by **services** (and in tests).

---

## `services/`

**Role:** Business logic. The core of the application.

- **Interfaces** (e.g. `EventService`, `TicketService`) — Contract for what the app can do. Used by controllers and tests.
- **`services/impl/`** — Implementations: orchestrate repositories, mappers, and validation; enforce rules; use `@Transactional` where needed.
- **EventAuthorizationService** — Used by `@PreAuthorize` (SpEL) to decide “is this user the organizer of this event?” So authorization is still “service-like” and testable.
- **JwtService** — Technical helper (create/parse JWT); used by the filter and auth.

So: **controllers call services; services call repositories and mappers**. All non-trivial logic lives here.

---

## `resources/`

**Role:** Static config and assets (non-Java).

- **`application.properties`** — Main config: DB URL, JPA, Flyway, JWT, OpenAPI paths, etc.
- **`db/migration/`** — Flyway SQL scripts (e.g. `V2__...`, `V8__...`). Schema and data changes in versioned order. No manual DDL in production; Flyway runs these on startup.

---

## How it fits together

```
  HTTP Request
       │
       ▼
  controllers     ← use DTOs, call services
       │
       ▼
  services        ← business logic, use repositories + mappers
       │
       ▼
  repositories    ← read/write entities
  mappers         ← entity ↔ DTO
       │
       ▼
  entities        ← map to DB tables
```

- **config** — Applied globally (security, JWT filter, CORS, OpenAPI, auditing).
- **exceptions** — Thrown by services; **controllers/advice** turns them into HTTP responses.
- **resources** — Configuration and DB migrations.

So yes: the structure is clear and maintainable. Each directory has a single, well-defined role, and dependencies flow in one direction (controllers → services → repositories / mappers → entities), which keeps the code easy to read and change.
