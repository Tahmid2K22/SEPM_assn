# SEPM_assn — University Management System

A minimal University Management System built with **Java Spring Boot**, covering:

- **REST API** — CRUD for `Admin`, `Teacher`, `Student` resources with proper REST conventions, JSON responses and HTTP status codes (200/201/204/404).
- **Spring Security** — login/logout form, BCrypt password hashing, and role-based access control (RBAC).
- **Dockerization** — multi-stage `Dockerfile` and `compose.yaml` running the app + PostgreSQL together.

## Access rules (RBAC)

| Role    | Can manage                          |
| ------- | ----------------------------------- |
| ADMIN   | admins, teachers, students          |
| TEACHER | teachers only                       |
| STUDENT | students only                       |

## REST endpoints

`/api/admins`, `/api/teachers`, `/api/students` — each supports:

| Method | Path          | Success | Notes                    |
| ------ | ------------- | ------- | ------------------------ |
| GET    | `/{resource}` | 200     | all records              |
| GET    | `/{resource}/{id}` | 200/404 | one record          |
| POST   | `/{resource}` | 201     | create (JSON body)       |
| PUT    | `/{resource}/{id}` | 200/404 | update (blank password = keep) |
| DELETE | `/{resource}/{id}` | 204/404 | delete               |

Plus `GET /api/me` — returns the logged-in user's email and role.
Passwords are never returned in any JSON response.

## Layer structure

```
com/example/webapp
├── config       # AppConfig (ModelMapper + BCrypt beans), SecurityConfig (RBAC)
├── controller   # REST controllers (@RestController)
├── dto          # Data transfer objects (ModelMapper mapping)
├── entity       # JPA entities
├── repository   # Spring Data JPA repositories
└── service      # business logic between controller and repository
```

## Run

```bash
docker compose up --build   # app on http://localhost:8080
```

Or locally against your own PostgreSQL: `mvnw spring-boot:run`.

## Creating the first user accounts

Accounts for all three roles live in the `admins`, `teachers` and `students`
tables (identical to the reference structure: entity → repository → service → controller).
There is no seeding logic in the code; create the initial accounts once with SQL:

```bash
docker compose exec postgres psql -U uni -d university
```

```sql
INSERT INTO admins   (name, email, password)                      VALUES ('Alice Admin','admin@uni.edu',  '$2a$10$Js/Z.y0Yn6VGEV32/mJnXeFjozbQVgzdK3DIht2CNdIdXbXmPwCLS');
INSERT INTO teachers (name, email, password, subject)             VALUES ('Bob Teacher','teacher@uni.edu','$2a$10$uY4TSIAL9V3DoeDbgee.meAFJE3YAlXZy8kaRwTFVfnKpiyrlcEey','Software Engineering');
INSERT INTO students (name, email, password, course)              VALUES ('Tahmid Chowdhury','tahmid@stud.kuet.ac.bd','$2a$10$NQlFSUfBMNAVPMaUKrMcFe64rhmt4h8o/E2RJyNEjV3y7CSgg0hkq','Software Engineering');
```

Passwords are BCrypt (`admin123`, `teacher123`, `student123`). New accounts can
then be created through the REST API (`POST /api/...`) by any permitted role.

