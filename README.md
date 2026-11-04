# GameFlix Authentication Service

This is my GameFlix authentication backend.

I originally made the authentication service for Module 4. For Module 5 I added Docker, Docker Compose, and GitHub Actions.

I used:

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- Docker
- Docker Compose
- GitHub Actions
- Postman

## Endpoints

The main endpoints are:

```text
POST /api/auth/register
POST /api/auth/login
GET /api/users/me
```

Register creates a new user.

Login checks the user's email and password and returns a JWT.

`/api/users/me` is protected and requires a valid Bearer token.

## Database

The project uses MySQL.

The database name is:

```text
gameflix_auth
```

Users are stored in the `users` table.

The table contains the user's:

- ID
- name
- email
- password hash
- creation date

Passwords are hashed with bcrypt before they are saved. The normal password is never stored in the database.

I used:

```properties
spring.jpa.hibernate.ddl-auto=update
```

so Hibernate can update the database without deleting my test data every time the application starts.

## JWT

The application uses JWT authentication.

The token contains the user's email and user ID.

My token expiration time is 30 minutes:

```properties
jwt.expiration-ms=1800000
```

The JWT secret is not hardcoded in the Java files.

For the Docker version, the secret is passed through an environment variable.

## Environment Variables

The project uses environment variables for the database credentials and JWT secret.

For Docker I keep the local values inside:

```text
.env
```

The `.env` file is ignored by Git and is not uploaded to GitHub.

Some of the environment variables are:

```text
MYSQL_DATABASE
MYSQL_ROOT_PASSWORD
MYSQL_USER
MYSQL_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MS
```

## Docker

For Module 5 I Dockerized the backend.

The project has:

```text
Dockerfile
compose.yaml
.dockerignore
```

Docker Compose runs two containers:

```text
Spring Boot backend
MySQL 8.0
```

The Spring Boot application runs on:

```text
http://localhost:8080
```

MySQL runs inside its own container.

The Spring Boot container connects to MySQL using:

```text
jdbc:mysql://mysql:3306/gameflix_auth
```

I start the containers with:

```text
docker compose up --build -d
```

I check them with:

```text
docker compose ps
```

When it is working, the Spring Boot container shows as running and the MySQL container shows as healthy.

I tested registration, login, and `/api/users/me` while the application was running through Docker.

## GitHub Actions

For the Module 5 CI/CD assignment I chose the GitHub Actions option instead of Coolify.

The workflow file is:

```text
.github/workflows/ci.yml
```

The workflow runs when I push changes to the `main` branch.

It:

1. Checks out the repository
2. Sets up Java 21
3. Builds the project with Maven
4. Builds the Docker image

The GitHub Actions workflow completed successfully.

## Testing

I used Postman to test the authentication service.

I tested:

- Registration - `201 Created`
- Duplicate email - `409 Conflict`
- Login - `200 OK`
- Wrong password - `401 Unauthorized`
- `/api/users/me` with a valid JWT - `200 OK`
- `/api/users/me` without a JWT - `401 Unauthorized`

For Module 5 I also tested the application while Spring Boot and MySQL were running in Docker.

I confirmed that:

- MySQL became healthy
- Spring Boot started successfully
- Spring Boot connected to the Docker MySQL database
- Registration worked
- Login worked
- JWT authentication worked
- GitHub Actions built the Maven project successfully
- GitHub Actions built the Docker image successfully

## Module 5

For Module 5 I chose:

```text
Option 1 - GitHub Actions CI/CD
```

I first tested the Docker version locally.

After that I pushed the Docker and GitHub Actions files to GitHub and confirmed that the workflow ran successfully.

## AI Assistance

I used ChatGPT to help with some of the Spring Boot setup, JWT setup, Docker setup, GitHub Actions, troubleshooting, and testing.

I still ran and tested everything myself and fixed problems as they came up.

Some of the problems I had to work through were getting Docker Desktop and WSL working, connecting Spring Boot to the Docker MySQL container, and fixing the JWT secret used by the Docker container.