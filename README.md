# Event Ticket Platform Backend

## Overview
The Event Ticket Platform backend is a Spring Boot service that powers ticket sales, user management, and event operations.
It is designed to be **production ready** with:
- Database migrations managed by Flyway
- Stateless JWT-based authentication
- Clear layered architecture (controllers → services → repositories)
- Automated tests and CI via GitHub Actions

For deeper technical details see `DOCUMENTATION.md`. For a breakdown of directory roles see `PROJECT_STRUCTURE.md`.

## Tech Stack
- Java 21 (Temurin)
- Spring Boot 3.5
- Flyway 11
- PostgreSQL (Neon in production) / H2 (tests)
- Maven build lifecycle

## Quick Start (Local Development)

1. **Clone and configure environment:**
   - Copy `.env.example` to `.env` and set values:
     ```bash
     DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>?sslmode=require
     JWT_SECRET=<a-long-random-secret>
     ```
   - The app reads these via `spring.config.import=optional:file:.env[.properties]`.

2. **Run the application (dev profile is optional):**
   ```bash
   mvn spring-boot:run
   # or
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Run the test suite:**
   ```bash
   mvn test
   ```

## Running in Production

1. **Build an executable JAR:**
   ```bash
   mvn -B clean package
   ```

2. **Set required environment variables (no defaults in prod):**
   ```bash
   export DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>?sslmode=require
   export JWT_SECRET=<strong-random-secret>
   export SPRING_PROFILES_ACTIVE=prod
   ```

3. **Run the JAR:**
   ```bash
   java -jar target/event-ticket-platform-0.0.1-SNAPSHOT.jar
   ```

In production, use a managed Postgres (e.g. Neon), configure proper CORS origins, and run behind HTTPS (via a reverse proxy or load balancer).

## Branching Model

We follow a GitFlow-inspired workflow:
- **main** – production-ready code only. Protected; merged from `dev` during releases.
- **dev** – integration branch for completed features. All PRs target this branch.
- **feature/** or **feature-** – short-lived branches for new work. Always branched from `dev`, merged back through PRs, then deleted.

## Developer Workflow

1. **Sync the integration branch:**
   ```bash
   git checkout dev
   git pull origin dev
   ```
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/<short-description>
   ```
3. **Implement changes and commit:**
   ```bash
   git add .
   git commit -m "feat: describe change"
   ```
4. **Push and open a Pull Request targeting `dev`:**
   ```bash
   git push -u origin feature/<short-description>
   ```
5. **After approval and passing checks**, merge the PR into `dev`, delete the feature branch, and repeat.

## Continuous Integration

GitHub Actions workflow: `.github/workflows/ci.yml`
- Triggers on pushes to `main`, `dev`, and `feature` branches, and on PRs into `main`/`dev`.
- Uses Temurin JDK 21 with Maven dependency caching.
- Runs the full Maven verify phase:
  ```bash
  mvn -B clean verify
  ```

## Pre-Commit Checklist

- **Code quality:** Lint/format Java sources (via IDE or Maven plugins).
- **Tests:** Ensure `mvn test` passes locally.
- **Migrations:** Validate Flyway migrations if schema changes were introduced.
- **Documentation:** Update `DOCUMENTATION.md`/`PROJECT_STRUCTURE.md` when APIs or architecture change.

## Useful Links

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Flyway Documentation](https://documentation.red-gate.com/fd)
- [GitHub Actions](https://docs.github.com/actions)
