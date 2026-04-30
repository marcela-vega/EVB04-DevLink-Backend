# DevLink Backend

Spring Boot + PostgreSQL backend para la plataforma DevLink, una red social para desarrolladores que buscan colaborar en proyectos.

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5 |
| Base de datos | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) |
| Documentación | SpringDoc OpenAPI 2 (Swagger UI) |
| Build | Maven (wrapper incluido) |
| Contenedor DB | Docker Compose |

---

## Estructura del proyecto

```
src/main/java/com/DevLink/backend/
├── BackendApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── HealthController.java
│   ├── NotificationController.java
│   ├── ProjectController.java
│   ├── TechnologyController.java
│   └── UserController.java
├── dto/                          # Request/Response DTOs
├── entity/
│   ├── Application.java
│   ├── Notification.java
│   ├── Project.java
│   ├── Role.java
│   ├── Technology.java
│   ├── User.java
│   └── enums/
│       ├── ApplicationStatus.java
│       ├── NotificationType.java
│       └── ProjectStatus.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BadRequestException.java
│   ├── NotFoundException.java
│   └── UnauthorizedException.java
├── repository/
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
└── service/
    ├── AuthService.java
    ├── MapperService.java
    ├── NotificationService.java
    ├── ProjectService.java
    ├── TechnologyService.java
    └── UserService.java
```

---

## Esquema de base de datos

| Tabla | Descripción |
|---|---|
| `users` | Perfiles de desarrolladores |
| `roles` | Roles del sistema (DEVELOPER, ADMIN, MODERATOR) |
| `user_roles` | M2M: Usuario ↔ Rol |
| `technologies` | Catálogo predefinido de tecnologías |
| `user_technologies` | M2M: Usuario ↔ Tecnología (stack del perfil) |
| `projects` | Proyectos creados por usuarios |
| `project_technologies` | M2M: Proyecto ↔ Tecnología requerida |
| `applications` | Postulaciones a proyectos |
| `notifications` | Notificaciones de usuario |

El esquema base se inicializa desde `db/init.sql`. Las columnas agregadas en esta rama se encuentran en `db/migration_development_rebase.sql` — ejecutar manualmente sobre una base ya existente.

Hibernate usa `ddl-auto=validate` (no crea ni modifica tablas).

---

## Autenticación

JWT stateless con Bearer token.

**Flujo:**
1. El usuario se registra o hace login → recibe un JWT y la fecha de expiración (`expiresAt`)
2. Incluye el token en cada request: `Authorization: Bearer <token>`
3. `JwtAuthenticationFilter` valida el token y establece el contexto de seguridad

**Rutas públicas:**
- `POST /api/auth/**`
- `GET /api/technologies`
- `GET /api/projects` y `GET /api/projects/{id}` (sólo publicados)
- `/swagger-ui/**` y `/v3/api-docs/**`

**Reglas de contraseña:** mínimo 8 caracteres, al menos 1 mayúscula y 1 número.

---

## Endpoints

### Auth
```
POST   /api/auth/register
POST   /api/auth/login
```

**Register — body:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string",
  "stack": ["1", "7"]
}
```

**Auth response:**
```json
{
  "token": "string",
  "expiresAt": "2026-04-30T12:00:00Z",
  "user": { ... }
}
```

### Usuarios
```
GET    /api/users/me             autenticado
PUT    /api/users/me             autenticado
GET    /api/users/{id}           público
```

**Update profile — body (todos los campos opcionales):**
```json
{
  "name": "string",
  "bio": "string",
  "githubUrl": "https://...",
  "gitlabUrl": "https://...",
  "stack": ["1", "7", "8"]
}
```

**User response:**
```json
{
  "id": 1,
  "name": "string",
  "email": "string",
  "role": "developer",
  "status": "active",
  "stack": ["1", "7"],
  "bio": "string",
  "avatar": null,
  "createdAt": "...",
  "projectsCount": 2,
  "collaborationsCount": 1,
  "githubUrl": "https://...",
  "gitlabUrl": null
}
```

### Tecnologías
```
GET    /api/technologies         público
```

### Proyectos
```
POST   /api/projects                                        crear proyecto
PUT    /api/projects/{id}                                   editar borrador (solo creador)
PUT    /api/projects/{id}/publish                           publicar al feed (solo creador)
POST   /api/projects/{id}/start-development                 iniciar desarrollo (solo creador)
POST   /api/projects/{id}/complete                          marcar como completado (solo creador)
GET    /api/projects                                        feed público — soporta ?technologyIds=1,2&page=0&size=10
GET    /api/projects/{id}                                   detalle (público si publicado)
```

**Create project — body:**
```json
{
  "title": "string",
  "description": "string",
  "stackRequired": ["1", "7"],
  "status": "draft | seeking_collaborators"
}
```

**Project response:**
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "stackRequired": ["1", "7"],
  "status": "draft | seeking_collaborators | in_development | completed",
  "creatorId": 1,
  "creator": { ... },
  "collaborators": [],
  "createdAt": "...",
  "updatedAt": "...",
  "startedAt": null,
  "completedAt": null,
  "applicationCount": 3,
  "canApply": true
}
```

**GET /api/projects — response paginado:**
```json
{
  "content": [...],
  "totalElements": 20,
  "number": 0,
  "size": 10,
  "totalPages": 2
}
```

### Postulaciones
```
POST   /api/projects/{id}/apply                                     postularse (autenticado, no creador)
GET    /api/projects/{id}/applications                              ver postulantes (solo creador)
PUT    /api/projects/{projectId}/applications/{appId}/accepted      aceptar postulante (solo creador)
PUT    /api/projects/{projectId}/applications/{appId}/rejected      rechazar postulante (solo creador)
GET    /api/applications/me                                         mis postulaciones (autenticado)
```

**Apply — body (opcional):**
```json
{
  "message": "string"
}
```

**Application response:**
```json
{
  "id": 1,
  "projectId": 1,
  "project": { ... },
  "applicantId": 2,
  "applicant": { ... },
  "message": "string",
  "status": "pending | accepted | rejected | closed",
  "createdAt": "...",
  "updatedAt": "..."
}
```

### Notificaciones
```
GET    /api/notifications            autenticado
POST   /api/notifications/{id}/read  marcar como leída (autenticado)
```

**Notification response:**
```json
{
  "id": 1,
  "type": "application_received",
  "title": "string",
  "message": "string",
  "read": false,
  "link": null,
  "createdAt": "..."
}
```

### Health
```
GET    /api/health               público
```

---

## Notificaciones automáticas

| Tipo | Cuándo se genera |
|---|---|
| `APPLICATION_RECEIVED` | Alguien se postula a un proyecto tuyo |
| `APPLICATION_ACCEPTED` | Tu postulación fue aceptada |
| `APPLICATION_REJECTED` | Tu postulación fue rechazada |
| `PROJECT_PUBLISHED` | Publicas un proyecto |
| `PROJECT_STARTED` | Marcas un proyecto como en desarrollo |
| `PROFILE_UPDATED` | Actualizas tu perfil |

---

## Estados del dominio

**Proyecto** — transiciones válidas:
```
DRAFT → LOOKING_FOR_COLLABORATORS → IN_DEVELOPMENT → COMPLETED
```

| Estado (back) | Estado (front) | Descripción |
|---|---|---|
| `DRAFT` | `draft` | Borrador, visible solo para el creador |
| `LOOKING_FOR_COLLABORATORS` | `seeking_collaborators` | Publicado, visible en el feed |
| `IN_DEVELOPMENT` | `in_development` | Desarrollo iniciado |
| `COMPLETED` | `completed` | Proyecto finalizado |

**Postulación:**

| Estado | Descripción |
|---|---|
| `PENDING` | Estado inicial al postularse |
| `ACCEPTED` | Creador aceptó al postulante |
| `REJECTED` | Creador rechazó la postulación |
| `CLOSED` | Postulación cerrada (proyecto fuera de búsqueda) |

---

## Convenciones de campos front ↔ back

Los DTOs de esta rama usan los nombres que espera el frontend:

| Campo backend (entidad) | Campo API (DTO) |
|---|---|
| `User.fullName` | `name` |
| `User.technologies` | `stack` (array de IDs como strings) |
| `User.active` | `status` (`"active"` / `"suspended"`) |
| `User.roles` | `role` (string singular en minúsculas, ej. `"developer"`) |
| `Project.technologies` | `stackRequired` (array de IDs como strings) |

---

## Configuración del entorno

Variables de entorno necesarias (ver `.env`):

```env
POSTGRES_DB=linkdev_db
POSTGRES_USER=linkdev_admin
POSTGRES_PASSWORD=

DB_URL=jdbc:postgresql://localhost:5432/linkdev_db
DB_USERNAME=linkdev_admin
DB_PASSWORD=

SERVER_PORT=8080

JWT_SECRET=
JWT_EXPIRATION_MS=86400000
```

---

## Levantar el proyecto localmente

**1. Iniciar PostgreSQL con Docker:**
```bash
docker compose up -d
```

**2. Aplicar migración (solo si la DB ya existía):**
```bash
psql -U linkdev_admin -d linkdev_db -f db/migration_development_rebase.sql
```

**3. Ejecutar el backend:**
```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

**4. Documentación interactiva (Swagger UI):**
```
http://localhost:8080/swagger-ui.html
```

---

## Reglas de negocio relevantes

- Las tecnologías del proyecto y del perfil provienen del catálogo predefinido en base de datos. El front envía sus IDs como strings.
- El filtro del feed público es AND: se devuelven proyectos que tengan **todas** las tecnologías seleccionadas.
- Los borradores son privados hasta que el creador los publica explícitamente.
- Un usuario no puede postularse a su propio proyecto.
- Cada par usuario-proyecto admite una sola postulación.
- Para postularse, el usuario debe tener al menos una tecnología en común con las requeridas por el proyecto.
- Solo el creador puede aceptar/rechazar postulaciones, iniciar desarrollo o completar el proyecto.
