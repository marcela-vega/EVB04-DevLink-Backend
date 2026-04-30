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

El esquema se inicializa desde `db/init.sql`. Hibernate usa `ddl-auto=validate` (no crea ni modifica tablas).

---

## Autenticación

JWT stateless con Bearer token.

**Flujo:**
1. El usuario se registra o hace login → recibe un JWT
2. Incluye el token en cada request: `Authorization: Bearer <token>`
3. `JwtAuthenticationFilter` valida el token y establece el contexto de seguridad

**Rutas públicas:**
- `POST /api/auth/**`
- `GET /api/technologies`
- `GET /api/projects` y `GET /api/projects/{id}` (sólo publicados)
- `GET /api/users/{id}`
- `/swagger-ui/**` y `/v3/api-docs/**`

**Reglas de contraseña:** mínimo 8 caracteres, al menos 1 mayúscula y 1 número.

---

## Historias de usuario incluidas (Sprint 1)

| HU | Descripción |
|---|---|
| HU01 | Registro de desarrollador |
| HU02 | Login con JWT |
| HU07 | Crear borrador de proyecto |
| HU08 | Publicar proyecto al feed |
| HU10 | Postularse a un proyecto publicado |
| HU15 | Filtrar proyectos por stack tecnológico (filtro AND) |
| HU17 | Editar perfil técnico |

---

## Endpoints

### Auth
```
POST   /api/auth/register
POST   /api/auth/login
```

### Usuarios
```
GET    /api/users/me             (autenticado)
PUT    /api/users/me             (autenticado)
GET    /api/users/{id}           (público)
```

### Tecnologías
```
GET    /api/technologies         (público)
```

### Proyectos
```
POST   /api/projects                        crear borrador
PUT    /api/projects/{id}                   editar borrador (solo creador)
PUT    /api/projects/{id}/publish           publicar borrador (solo creador)
GET    /api/projects                        feed público — soporta ?technologyIds=1&technologyIds=2
GET    /api/projects/{id}                   detalle (público si publicado)
POST   /api/projects/{id}/apply             postularse (autenticado, no creador)
GET    /api/projects/{id}/applications      ver postulantes (solo creador)
```

### Notificaciones
```
GET    /api/notifications/me     (autenticado)
```

### Health
```
GET    /api/health               (público)
```

---

## Notificaciones automáticas

| Tipo | Cuándo se genera |
|---|---|
| `APPLICATION_RECEIVED` | Alguien se postula a un proyecto tuyo |
| `PROJECT_PUBLISHED` | Publicas un proyecto |
| `PROFILE_UPDATED` | Actualizas tu perfil |

---

## Estados del dominio

**Proyecto:**
- `DRAFT` — borrador, visible solo para el creador
- `LOOKING_FOR_COLLABORATORS` — publicado, visible en el feed público

**Postulación:**
- `PENDING` — estado inicial al postularse

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

**2. Ejecutar el backend:**
```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

**3. Documentación interactiva (Swagger UI):**
```
http://localhost:8080/swagger-ui.html
```

---

## Reglas de negocio relevantes

- Las tecnologías del proyecto y del perfil provienen del catálogo predefinido en base de datos.
- El filtro del feed público es AND: se devuelven proyectos que tengan **todas** las tecnologías seleccionadas.
- Los borradores son privados hasta que el creador los publica explícitamente.
- Un usuario no puede postularse a su propio proyecto.
- Cada par usuario-proyecto admite una sola postulación.
