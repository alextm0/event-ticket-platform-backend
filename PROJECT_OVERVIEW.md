# Event Ticket Platform - Complete Project Overview

## 🎯 **What This Application Does**

This is a **Spring Boot REST API** for an **Event Ticket Platform** that manages:
- **Users** (Organizers, Staff, Attendees)
- **Events** (creation, publishing, cancellation)
- **Tickets** (types, sales, QR codes)
- **Orders** (purchases, payment tracking)
- **Ticket Validation** (QR scanning, manual entry)

---

## 🏗️ **Architecture Overview**

### **Technology Stack**
- **Java 21** - Modern Java with records, pattern matching
- **Spring Boot 3.5.7** - Enterprise framework
- **PostgreSQL** - Database (Neon in production)
- **Flyway 11** - Database migrations
- **JPA/Hibernate** - Object-relational mapping
- **MapStruct** - DTO mapping (compile-time code generation)
- **Lombok** - Reduces boilerplate code
- **Spring Security Crypto** - Password hashing (BCrypt)
- **OpenAPI/Swagger** - API documentation

### **Project Structure**
```
src/main/java/com/project/event_ticket_platform/
├── Application.java                    # Main entry point
├── config/                             # Configuration classes
│   ├── PasswordConfig.java            # BCrypt password encoder
│   ├── JpaAuditingConfig.java         # Automatic timestamp management
│   └── OpenApiConfig.java             # Swagger/OpenAPI setup
├── controllers/                        # REST API endpoints
│   ├── UserController.java            # User management
│   ├── EventController.java           # Event management
│   ├── HealthController.java          # Health check
│   └── advice/
│       └── GlobalExceptionHandler.java # Error handling
├── services/                           # Business logic
│   ├── UserService.java               # User operations
│   └── impl/
│       └── EventServiceImpl.java      # Event operations
├── repositories/                       # Data access layer
│   ├── UserRepository.java
│   └── EventRepository.java
├── entities/                           # Database entities (JPA)
│   ├── User.java
│   ├── Event.java
│   ├── TicketType.java
│   ├── TicketOrder.java
│   ├── Ticket.java
│   ├── QrCode.java
│   ├── EventStaff.java
│   └── TicketValidation.java
├── dtos/                               # Data Transfer Objects
│   ├── CreateUserRequest.java
│   ├── UserResponse.java
│   ├── CreateEventRequest.java
│   ├── EventResponse.java
│   └── UpdateEventRequest.java
├── mappers/                            # MapStruct mappers
│   └── EventMapper.java
└── exceptions/                         # Custom exceptions
    ├── EmailAlreadyExistsException.java
    ├── EventNotFoundException.java
    ├── EventValidationException.java
    └── OrganizerNotFoundException.java
```

---

## 📊 **Database Schema**

### **Core Tables**

1. **`users`** - System users
   - `id` (UUID, primary key)
   - `name`, `email` (unique), `password_hash`
   - `role` (ORGANIZER, STAFF, ATTENDEE)
   - `created_at`, `updated_at`

2. **`events`** - Events created by organizers
   - `id` (UUID, primary key)
   - `organizer_id` → references `users(id)`
   - `title`, `description`, `location`
   - `start_time`, `end_time`
   - `status` (DRAFT, PUBLISHED, CANCELLED)

3. **`ticket_types`** - Ticket catalog for events
   - `id` (UUID, primary key)
   - `event_id` → references `events(id)`
   - `name`, `description`, `price`
   - `total_quantity`, `sold_count`
   - `is_active`
   - **Constraint**: `sold_count <= total_quantity`

4. **`orders`** - Ticket purchase orders
   - `id` (UUID, primary key)
   - `user_id` → references `users(id)`
   - `buyer_name`, `buyer_email`
   - `total_amount`
   - `status` (PENDING, PAID, CANCELLED)

5. **`tickets`** - Individual tickets
   - `id` (UUID, primary key)
   - `order_id` → references `orders(id)`
   - `ticket_type_id` → references `ticket_types(id)`
   - `qr_code_id` → references `qr_codes(id)` (unique)
   - `status` (PURCHASED, CHECKED_IN)
   - `checked_in_at`

6. **`qr_codes`** - QR code lifecycle
   - `id` (UUID, primary key)
   - `generated_date_time`
   - `status` (ACTIVE, EXPIRED)
   - `updated_at`

7. **`event_staff`** - Staff assignments to events
   - `id` (UUID, primary key)
   - `event_id` → references `events(id)`
   - `staff_id` → references `users(id)`
   - **Unique constraint**: (event_id, staff_id)

8. **`ticket_validations`** - Validation audit log
   - `id` (UUID, primary key)
   - `ticket_id` → references `tickets(id)`
   - `status` (VALID, INVALID, EXPIRED)
   - `validation_date_time`
   - `validation_method` (QR_SCAN, MANUAL)

---

## 🔌 **API Endpoints**

### **Base URL**: `http://localhost:8080/api/v1`

### **1. User Management** (`/api/v1/users`)
- **POST** `/api/v1/users` - Create a new user
  ```json
  {
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "ATTENDEE"
  }
  ```
- **GET** `/api/v1/users` - List all users

### **2. Event Management** (`/api/v1/events`)
- **POST** `/api/v1/events` - Create a new event
  ```json
  {
    "organizerId": "uuid",
    "title": "Concert",
    "description": "Music event",
    "location": "Berlin",
    "startTime": "2025-12-01T19:00:00Z",
    "endTime": "2025-12-01T23:00:00Z",
    "status": "DRAFT"
  }
  ```
- **GET** `/api/v1/events?page=0&size=20` - List events (paginated)
- **PUT** `/api/v1/events/{eventId}` - Update an event
- **DELETE** `/api/v1/events/{eventId}` - Delete an event

### **3. Health Check** (`/api/v1/health`)
- **GET** `/api/v1/health` - Returns `{"status": "UP", "timestamp": "..."}`

### **4. API Documentation**
- **GET** `/swagger-ui.html` - Swagger UI
- **GET** `/api/v1/openapi` - OpenAPI JSON

---

## 🔄 **Key Business Logic**

### **Event Lifecycle**
1. **DRAFT** → Events start as drafts
2. **PUBLISHED** → Organizers can publish (one-way transition)
3. **CANCELLED** → Published events can be cancelled

**Valid Transitions:**
- DRAFT → PUBLISHED ✅
- PUBLISHED → CANCELLED ✅
- CANCELLED → PUBLISHED ❌ (not allowed)

### **Event Validation Rules**
- Start time must be in the future (for new events)
- End time must be after start time
- Title, description, location are required
- Organizer must exist in the system

### **User Registration**
- Email must be unique
- Password is hashed with BCrypt
- Default role is ATTENDEE if not specified
- Password must be at least 8 characters

---

## 🔧 **How Things Work Together**

### **1. Request Flow**
```
Client Request
    ↓
Controller (validates input)
    ↓
Service (business logic)
    ↓
Repository (database access)
    ↓
Entity (JPA mapping)
    ↓
Database (PostgreSQL)
```

### **2. Data Flow Example: Creating an Event**

1. **Client** sends POST to `/api/v1/events` with JSON
2. **EventController** receives request, validates with `@Valid`
3. **EventService.createEvent()**:
   - Checks if organizer exists
   - Uses **EventMapper** (MapStruct) to convert DTO → Entity
   - Validates business rules (dates, status transitions)
   - Saves to database via **EventRepository**
4. **EventMapper** converts Entity → Response DTO
5. **Controller** returns 201 Created with event data

### **3. Automatic Features**

- **JPA Auditing**: `created_at` and `updated_at` are automatically managed
- **Flyway Migrations**: Database schema is versioned and applied on startup
- **Exception Handling**: Global exception handler converts exceptions to HTTP status codes
- **Password Hashing**: BCrypt automatically hashes passwords

---

## 🧪 **Testing**

### **Test Structure**
- **Unit Tests**: Service layer (e.g., `EventServiceImplTest`)
- **Integration Tests**: Controller layer (e.g., `EventControllerTest`, `UserControllerTest`)
- **Test Database**: H2 (in-memory) for tests

### **Running Tests**
```bash
mvn test
```

---

## ⚙️ **Configuration**

### **application.properties**
- **Database**: Configured via `DATABASE_URL` environment variable
- **Flyway**: Enabled, migrations in `classpath:db/migration`
- **JPA**: Validates schema (doesn't auto-create tables)
- **OpenAPI**: Swagger UI at `/swagger-ui.html`

### **Environment Variables**
Create a `.env` file:
```ini
DATABASE_URL=jdbc:postgresql://host:port/database?user=user&password=pass&sslmode=require
```

---

## 🚨 **Current Issues & Notes**

### **1. Flyway Maven Plugin Configuration**
The `pom.xml` has Flyway plugin configured to use environment variables:
```xml
<configuration>
    <url>${env.FLYWAY_URL}</url>
    <user>${env.FLYWAY_USER}</user>
    <password>${env.FLYWAY_PASSWORD}</password>
</configuration>
```

**Problem**: The plugin is missing the PostgreSQL driver dependency. You need to add:
```xml
<dependencies>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
        <version>${flyway.version}</version>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>${postgresql.version}</version>
    </dependency>
</dependencies>
```

### **2. Missing Migration Files**
The project layout shows `V3__Add_ticket_inventory_check.sql` and `V4__Single_name_users.sql`, but they don't exist. The inventory check is already in `V2`.

### **3. Missing Controllers**
The database schema supports:
- Ticket Types
- Orders
- Tickets
- QR Codes
- Staff Assignments
- Ticket Validations

But there are **no controllers** for these entities yet. Only `UserController` and `EventController` exist.

---

## 📝 **What's Missing (Not Yet Implemented)**

1. **Ticket Type Management** - Create/update ticket types for events
2. **Order Management** - Purchase tickets, create orders
3. **Ticket Management** - Generate QR codes, manage tickets
4. **Staff Management** - Assign staff to events
5. **Ticket Validation** - QR code scanning, manual validation
6. **Authentication/Authorization** - Currently no security (no login, no JWT)
7. **Payment Integration** - Orders are tracked but no payment processing

---

## 🎓 **Key Patterns Used**

1. **DTO Pattern** - Separate request/response objects from entities
2. **Repository Pattern** - Data access abstraction
3. **Service Layer** - Business logic separation
4. **MapStruct** - Compile-time DTO mapping (no reflection overhead)
5. **JPA Auditing** - Automatic timestamp management
6. **Global Exception Handling** - Centralized error responses
7. **RESTful Design** - Standard HTTP methods and status codes

---

## 🔍 **How to Understand the Code Flow**

1. **Start with Controllers** - See what endpoints are available
2. **Follow to Services** - Understand business logic
3. **Check Entities** - See database structure
4. **Review Migrations** - Understand schema evolution
5. **Look at Tests** - See expected behavior

---

## 🚀 **Next Steps**

1. Fix Flyway Maven plugin configuration
2. Implement missing controllers (Orders, Tickets, etc.)
3. Add authentication/authorization
4. Implement ticket purchase flow
5. Add QR code generation
6. Implement ticket validation endpoints

---

This is a well-structured Spring Boot application with a clear separation of concerns, following best practices for REST API development!

