# Library Management System

A backend-focused Library Management System built with Java 17 and Spring Boot. The application provides REST APIs for library operations, role-based access control, MySQL persistence, and Redis integration.

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot
- **Security:** Spring Security
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** MySQL
- **Caching:** Redis
- **Containerization:** Docker
- **Build Tool:** Maven
- **API Style:** REST

## Features

- Library and book management
- Student and administrator roles
- Book borrowing and return workflows
- Configurable borrowing duration and overdue fines
- Authentication and authorization
- Redis integration for user details caching
- MySQL persistence using Spring Data JPA / Hibernate

## Prerequisites

- JDK 17
- MySQL Server
- Docker Desktop or Docker Engine
- Maven or Maven Wrapper
- Git

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR-USERNAME/Library-Management-System.git
cd Library-Management-System
```

Replace `YOUR-USERNAME` with your GitHub username.

### 2. Configure MySQL

Ensure MySQL Server is running and configure the database connection.

Example database URL:

```text
jdbc:mysql://localhost:3306/library_management?createDatabaseIfNotExist=true
```

Set your database URL, username, and password through environment variables or your IDE's run configuration.

### 3. Start Redis Using Docker

Ensure Docker is running, then execute:

```bash
docker run -d --name library-redis -p 6379:6379 redis:7-alpine
```

Verify that the container is running:

```bash
docker ps
```

Redis will be available at `localhost:6379`.

If the container already exists, start it with:

```bash
docker start library-redis
```

### 4. Configure the Application

The main configuration file is located at:

`src/main/resources/application.properties`

Configure the MySQL and Redis connection settings according to your local environment.

Spring Boot does not automatically load `.env` files without an appropriate loader or run configuration.

Never commit database passwords, access tokens, or local `.env` files.

### 5. Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

On Windows, if the Maven Wrapper is included:

```bash
mvnw.cmd spring-boot:run
```

### 6. Run Tests

```bash
mvn test
```

## Future Improvements

- Integrate Apache Kafka for asynchronous messaging and event-driven workflows
- Expand unit and integration test coverage
- Add API documentation using OpenAPI/Swagger
- Containerize the application and database setup
- Configure continuous integration using GitHub Actions

## Author

Developed as a Java backend portfolio project to demonstrate Spring Boot development, REST API design, database integration, security, caching, and containerized dependency management.
