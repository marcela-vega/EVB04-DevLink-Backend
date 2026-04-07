# DevLink Backend — Hito 1

Spring Boot + PostgreSQL backend for the Spring 1 MVP.

## Included Hito 1 stories
- HU01 Register developer
- HU02 Login
- HU07 Create project draft
- HU08 Publish project to feed
- HU10 Apply to projects
- HU15 Filter projects by technology stack (AND filter)
- HU17 Edit technical profile

## Run PostgreSQL with Docker
```bash
docker compose up -d
```

## Run backend
```bash
./mvnw spring-boot:run
```
On Windows:
```bash
mvnw.cmd spring-boot:run
```

## Main endpoints
### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Users
- `GET /api/users/me`
- `PUT /api/users/me`
- `GET /api/users/{id}`

### Technologies
- `GET /api/technologies`

### Projects
- `POST /api/projects` create draft
- `PUT /api/projects/{id}` update draft
- `PUT /api/projects/{id}/publish` publish draft
- `GET /api/projects` public feed. Supports `?technologyIds=1&technologyIds=2`
- `GET /api/projects/{id}` project details
- `POST /api/projects/{id}/apply` apply to a published project
- `GET /api/projects/{id}/applications` creator-only view of applicants

### Notifications
- `GET /api/notifications/me`

## Notes
- Registration password rule: at least 8 characters, 1 uppercase, 1 number.
- Stack uses predefined technologies from database.
- Public project filtering is AND-based, matching all selected technologies.
- Draft projects are private to their creator.
