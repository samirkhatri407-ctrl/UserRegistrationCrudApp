# User Registration CRUD Application

## Project overview

A containerized, modular web application for registering and managing users. The frontend is built with React, the backend is a Java Spring Boot REST API, and PostgreSQL stores the application data. Spring Data JPA provides persistence and Hibernate is used as the JPA implementation.

The application is organized as separate frontend, backend, and database modules so each part has a clear responsibility and can be developed and maintained independently.

## Technology stack

| Module | Technology |
|---|---|
| Frontend | React |
| Backend | Java, Spring Boot, Spring Web |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Containers | Docker, Docker Compose |

## Features

- Create a user registration with the user's name, email, and password.
- View registered users.
- View an individual user's details.
- Update a user's details.
- Delete a user registration.
- Validate required fields and reject duplicate email addresses.
- Explore and try the API through Swagger UI.

Passwords must be stored as secure password hashes, never as plain text. The user API should not return password hashes.

## Module layout

```text
user-registration-crud/
├── frontend/       # React user interface
├── backend/        # Spring Boot REST API and persistence logic
├── compose.yaml    # Frontend, backend, and PostgreSQL services
├── .env.example    # Example local database setting
└── README.md
```

The frontend, backend, and database run as separate Docker services. PostgreSQL uses its official image; no separate database application module is needed.

### Backend code map

The Java code uses a small number of layers. Each class has one main job:

| Class or package | Beginner-friendly purpose |
|---|---|
| `UserController` | Receives web requests and returns HTTP responses |
| `UserService` | Applies user and email rules before saving changes |
| `UserRepository` | Reads and writes users through Spring Data JPA |
| `User` | Describes the database table and its columns |
| `UserRegistrationRequest`, `UserUpdateRequest` | Define and validate data accepted from the frontend |
| `UserResponse` | Defines safe user data returned by the API |
| `ApiExceptionHandler` | Turns common errors into readable HTTP responses |
| `PasswordConfig` | Creates the password-hashing service |

The normal request path is **Controller → Service → Repository → PostgreSQL**. Keeping these jobs separate makes the application easier to learn, test, and change.

## Architecture

```text
Browser
  │
  ▼
React frontend
  │ HTTP/JSON
  ▼
Spring Boot REST API
  │ Spring Data JPA / Hibernate
  ▼
PostgreSQL database
```

The frontend communicates with the backend over HTTP using JSON. The backend validates requests, applies application rules, and accesses PostgreSQL through JPA repositories. Database credentials and other environment-specific settings should be supplied through environment variables rather than committed to source control.

## API outline

The backend can expose a user resource under `/api/users`:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/users` | Register a user |
| `GET` | `/api/users` | List users |
| `GET` | `/api/users/{id}` | Get one user |
| `PUT` | `/api/users/{id}` | Update a user |
| `DELETE` | `/api/users/{id}` | Delete a user |

Registration requires `firstName`, `lastName`, `email`, `password`, and `dateOfBirth`. Profile fields may be omitted. Passwords are accepted only during registration, hashed with BCrypt, and never included in API responses. Updating a user's profile does not change their password.

The `PUT /api/users/{id}` update request accepts the profile fields and an `active` value; it does not accept a password.

## Data model

The initial `User` entity is expected to contain these profile and account fields:

| Field | Description |
|---|---|
| `id` | Generated primary key |
| `firstName` | User's first or given name |
| `lastName` | User's last or family name |
| `email` | Required, unique email address |
| `passwordHash` | Securely hashed password; never return it from the API |
| `dateOfBirth` | User's date of birth |
| `phoneNumber` | Contact phone number, if provided |
| `addressLine1` | Primary street address |
| `addressLine2` | Apartment, suite, or additional address details; optional |
| `city` | City or locality |
| `stateProvince` | State, province, or region |
| `postalCode` | ZIP or postal code |
| `country` | Country or territory |
| `createdAt` | Registration timestamp |
| `updatedAt` | Last-update timestamp |
| `active` | Whether the user account is active |

This provides 16 stored fields, including identity, contact, location, and account details. Optional profile fields such as phone number and the second address line may be left empty. The API validates required fields, email format and uniqueness, past date of birth, and field lengths. Email addresses are normalized to lowercase.

## Run with Docker

Requirements: Docker Desktop with Docker Compose.

1. Copy `.env.example` to `.env` and set a local database password:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Build and start the frontend, backend, and database:

   ```powershell
   docker compose up --build
   ```

3. Open the application at <http://localhost:3000>. Swagger API documentation is available at <http://localhost:8080/swagger-ui.html> or through the frontend's API documentation link.

4. Stop the application with `Ctrl+C`, then run:

   ```powershell
   docker compose down
   ```

The PostgreSQL data is kept in the `postgres_data` volume when containers stop. To delete the database data too, run `docker compose down --volumes`.

## Run backend tests with Maven

Requirements: JDK 21 and Maven 3.9 or newer. From the project root:

```powershell
cd backend
mvn test
```

Tests use an in-memory H2 database, so PostgreSQL and Docker are not required to run them. The integration tests cover registration, response password safety, case-insensitive duplicate email rejection, input validation, profile updates, and deletion.

To start only the backend locally, run PostgreSQL separately and configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`, then use `mvn spring-boot:run` from `backend`.

## API examples

Register a user with `POST /api/users`:

```json
{
  "firstName": "Taylor",
  "lastName": "Jordan",
  "email": "taylor@example.com",
  "password": "use-a-strong-password",
  "dateOfBirth": "1995-06-15",
  "phoneNumber": "555-0100",
  "city": "Toronto",
  "country": "Canada"
}
```

The response contains the user's profile and timestamps, but never the password or its hash. Invalid fields return HTTP `400`, duplicate email returns `409`, and an unknown user ID returns `404`.

## Initial scope

This is a local-development CRUD starter. Login, authentication, authorization, email verification, password reset, production deployment configuration, and advanced user roles are not included. Do not expose it to the public internet as-is. Before production use, add authentication and authorization, use a managed secret store, and replace automatic JPA schema updates with a database migration tool such as Flyway.
