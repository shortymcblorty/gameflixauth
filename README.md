# GameFlix Authentication Service

## Project Overview

This project is the GameFlix User Authentication Service for Module 4. It is built using Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, and MySQL.

The purpose of this service is to allow GameFlix users to register an account, log in, receive a signed JWT, and use that token to access a protected user profile endpoint.

The required endpoints are:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`

---

## Technologies Used

- Java 21
- Spring Boot 4.1.1
- Spring Security
- Spring Data JPA
- MySQL 8.0.46
- Maven
- JJWT
- Postman
- IntelliJ IDEA

---

## Required Endpoints

### POST /api/auth/register

Creates a new GameFlix user.

Example request:

```json
{
  "name": "Test User",
  "email": "testuser@example.com",
  "password": "GameFlix123!"
}
```

Possible responses:

- `201 Created` - User successfully registered
- `400 Bad Request` - Invalid input
- `409 Conflict` - Email is already registered

The password is hashed before it is stored in MySQL. The plaintext password and password hash are not returned in the response.

### POST /api/auth/login

Authenticates a GameFlix user using their email and password.

Example request:

```json
{
  "email": "testuser@example.com",
  "password": "GameFlix123!"
}
```

Possible responses:

- `200 OK` - Login is successful and a signed JWT is returned
- `401 Unauthorized` - The email or password is incorrect

A failed login returns the same generic message regardless of whether the email or password caused the failure.

### GET /api/users/me

Returns the profile of the currently authenticated user.

The request requires a valid JWT:

```text
Authorization: Bearer YOUR_JWT_TOKEN
```

Possible responses:

- `200 OK` - Valid JWT and user profile returned
- `401 Unauthorized` - Token is missing, invalid, tampered with, or expired

The user is identified from the authenticated JWT instead of passing a user ID in the URL.

---

## How to Run the Project

### 1. Requirements

Make sure the following are installed:

- Java 21
- MySQL Server
- Maven or the included Maven Wrapper

### 2. Create the MySQL Database

Open MySQL Workbench and run:

```sql
CREATE DATABASE gameflix_auth;
```

The project expects this database name:

```text
gameflix_auth
```

### 3. Create Local Configuration

Create:

```text
src/main/resources/application-local.properties
```

Add your own MySQL credentials and JWT secret:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gameflix_auth
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

jwt.secret=YOUR_BASE64_JWT_SECRET
```

The JWT secret should be a securely generated Base64 value.

`application-local.properties` is listed in `.gitignore`, so the real MySQL password and JWT signing secret are not committed to the repository.

The normal `application.properties` contains the non-secret configuration:

```properties
spring.application.name=gameflixauth

spring.config.import=optional:classpath:application-local.properties

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.expiration-ms=1800000
```

### 4. Run the Application

Run:

```text
GameflixauthApplication.java
```

The service runs locally at:

```text
http://localhost:8080
```

---

## Database Schema

GameFlix users are stored in MySQL using Spring Data JPA.

The `users` table contains:

- `id` - Primary key that automatically increments
- `created_at` - Date and time the account was created
- `email` - User email with a database-level unique constraint
- `name` - User's name
- `password` - Stores the bcrypt password hash

The password column uses `VARCHAR(255)` so the complete password hash can be stored without being truncated. Email uniqueness is enforced directly by MySQL instead of relying only on an application-side check.

The following schema was taken from the actual MySQL database using:

```sql
SHOW CREATE TABLE users;
```

```sql
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB
  AUTO_INCREMENT=3
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
```

---

## Password Hashing

The application uses bcrypt through Spring Security's delegating `PasswordEncoder`.

The password is hashed before the User entity is saved to MySQL. A plaintext password is never stored in the database, returned in an API response, or intentionally written to the application logs.

Spring Security's delegating password encoder uses bcrypt with its default work factor of `10`. I kept this work factor because it provides adaptive password hashing while still allowing login requests to complete quickly enough for this project. Bcrypt was chosen because it is designed specifically for password storage and is directly supported by Spring Security.

The registration request also limits passwords to a maximum of 64 characters.

A stored password looks similar to:

```text
{bcrypt}$2a$10$...
```

instead of the user's original password.

---

## JPA ddl-auto Setting

The project uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

I chose `update` because it allows Hibernate to create or update the database schema while keeping existing users and testing data between application runs. I did not use `create-drop` because that would erase the test data whenever the application stops.

---

## JWT Design

The login endpoint creates a signed JSON Web Token after successful authentication.

The JWT contains the authenticated user's email as the subject and the user ID as a claim. Passwords and other confidential information are not stored in the token.

The token includes:

- `sub` - User email
- `userId` - User ID
- `iat` - Time the token was issued
- `exp` - Expiration time

The token signature is verified by the server before the request is treated as authenticated.

### JWT Lifetime

The JWT lifetime is:

```text
30 minutes
```

The configuration value is:

```properties
jwt.expiration-ms=1800000
```

I chose 30 minutes because it gives a user enough time to use GameFlix without needing to constantly log in again. It also limits how long a stolen access token would remain useful compared to using a token that lasts for many hours or days.

---

## JWT Secret

The JWT signing secret is not hardcoded in the Java source code.

The real secret is stored in:

```text
src/main/resources/application-local.properties
```

This file is ignored by Git.

The repository includes:

```text
application.properties.example
```

with placeholder values so someone can see what settings are required without exposing the real credentials or JWT secret.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gameflix_auth
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

jwt.secret=YOUR_BASE64_JWT_SECRET
jwt.expiration-ms=1800000
```

---

## CORS Configuration

CORS is restricted to the GameFlix frontend development origin:

```text
http://localhost:3000
```

I did not allow every origin using `*`. Limiting CORS to the expected GameFlix frontend reduces which browser applications are allowed to make cross-origin requests to the authentication service.

---

## Test Evidence

The API was tested using Postman.

### Test 1 - Successful Registration

Endpoint:

```text
POST /api/auth/register
```

Result:

```text
201 Created
```

A new user was created and the response returned the user's ID, name, email, and creation time without returning the password or password hash.

### Test 2 - Duplicate Email Rejection

Endpoint:

```text
POST /api/auth/register
```

Result:

```text
409 Conflict
```

The service rejected a second registration using the same email address. Email uniqueness is enforced by the MySQL unique constraint.

### Test 3 - Successful Login

Endpoint:

```text
POST /api/auth/login
```

Result:

```text
200 OK
```

The correct email and password returned a signed JWT.

### Test 4 - Wrong Password

Endpoint:

```text
POST /api/auth/login
```

Result:

```text
401 Unauthorized
```

The service returned the generic message:

```text
Invalid email or password
```

### Test 5 - Protected Route With Valid JWT

Endpoint:

```text
GET /api/users/me
```

Result:

```text
200 OK
```

A valid Bearer token allowed access to the authenticated user's profile.

### Test 6 - Protected Route Without JWT

Endpoint:

```text
GET /api/users/me
```

Result:

```text
401 Unauthorized
```

The same protected route was rejected when no JWT was included.

Screenshots of these tests are included in the Word submission document.

---

## MySQL Password Hash Verification

After registering the test user, I ran:

```sql
SELECT id, email, password FROM users;
```

The MySQL result showed that the `password` column contained a value beginning with:

```text
{bcrypt}$2a$10$...
```

instead of the plaintext password `GameFlix123!`.

This confirms that the password was hashed before it was persisted.

The MySQL result is included as a screenshot in the Word submission document.

---

## Module 3 Design Change

During Module 3, my architecture diagram and ERD were not completely consistent with each other. My original architecture showed User, Catalog, and Review as separate services, while the ERD later grouped most of the GameFlix data into the main Spring Boot application.

For Module 4, I made the implementation more specific by treating user registration and login as a separate authentication microservice. The `User` entity and `users` table are the source of truth for this service. The other GameFlix features such as the catalog and reviews can remain part of the main application while authentication stays separated.

---

## AI Assistance

I used ChatGPT as an AI assistant while developing this project.

AI helped me:

- Organize the Spring Boot project structure
- Generate some initial Java code
- Explain Spring Security and JWT concepts
- Troubleshoot MySQL and configuration problems
- Plan the Postman tests
- Compare the finished work to the assignment requirements

I reviewed and tested the generated code instead of assuming that it was correct.

One AI-generated instruction I had to change was the PowerShell command used to generate the JWT signing secret. The first command used `RandomNumberGenerator.Fill()`, but that method was not supported by the PowerShell/.NET environment on my computer and produced an error. I changed it to create a `RandomNumberGenerator` object and call `GetBytes()` instead. This generated the random Base64 secret correctly and prevented me from using the invalid value created by the failed command.

---

## Security Decisions

The project follows these security rules:

- Passwords are never stored as plaintext.
- Bcrypt is used for password hashing.
- Password hashes are never returned in API responses.
- Email uniqueness is enforced at the database level.
- Failed logins return one generic error message.
- JWTs contain an expiration claim.
- JWT signatures are verified before protected routes are accessed.
- The JWT signing secret is not hardcoded.
- Database credentials are not committed to Git.
- Confidential information is not stored in JWT claims.
- CORS is restricted to `http://localhost:3000`.
- Protected routes return `401 Unauthorized` when authentication is missing or invalid.

---

## Current Project Status

```text
[x] Java 21 installed
[x] Spring Boot 4.1.1 project created
[x] MySQL database created
[x] Spring Data JPA connected to MySQL
[x] User entity created
[x] Database email uniqueness constraint created
[x] Password column configured as VARCHAR(255)
[x] UserRepository created
[x] Registration validation created
[x] Bcrypt password hashing configured
[x] Registration endpoint completed
[x] Duplicate email handling completed
[x] Login endpoint completed
[x] JWT generation completed
[x] JWT validation completed
[x] Protected /api/users/me endpoint completed
[x] CORS restricted to a named origin
[x] Six required Postman tests completed
[x] MySQL hash evidence captured
[x] Database schema added to README
[x] README placeholders replaced
[ ] Word screenshot document finalized
[ ] GitHub repository created and pushed
[ ] GitHub URL added to first page of Word document
```

---

## Final Submission Checklist

Before submitting, confirm:

- [x] `POST /api/auth/register` returns the required status codes
- [x] `POST /api/auth/login` returns the required status codes
- [x] `GET /api/users/me` requires a valid JWT
- [x] Passwords are hashed in MySQL
- [x] Email uniqueness is enforced by the database
- [x] JWT includes an expiration claim
- [x] JWT signature is validated
- [x] JWT secret is externalized
- [x] CORS uses a named frontend origin
- [x] `application-local.properties` is ignored by Git
- [x] `application.properties.example` is included
- [x] Database schema is included in README
- [x] Hashing algorithm and work factor are justified
- [x] `ddl-auto` choice is explained
- [x] JWT lifetime is justified
- [x] Module 3 design change is documented
- [x] AI assistance and one changed AI suggestion are documented
- [x] All six required Postman tests have evidence
- [x] MySQL password hash evidence is captured
- [ ] Word screenshot document is finalized
- [ ] GitHub repository is created and pushed
- [ ] GitHub repository URL is added to the first page of the Word document