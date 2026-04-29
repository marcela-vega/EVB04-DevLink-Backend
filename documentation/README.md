# DevLink API - Documentación

## Endpoints

| # | Método | URL | Descripción | Auth |
|---|--------|-----|-------------|------|
| 1 | POST | /api/auth/register | Registrar usuario | No |
| 2 | POST | /api/auth/login | Iniciar sesión | No |
| 3 | GET | /api/health | Health check | Sí |
| 4 | GET | /api/technologies | Listar tecnologías | No |
| 5 | GET | /api/users/me | Mi perfil | Sí |
| 6 | PUT | /api/users/me | Actualizar perfil | Sí |
| 7 | GET | /api/users/{id} | Perfil público | Sí |
| 8 | POST | /api/projects | Crear proyecto (borrador) | Sí |
| 9 | PUT | /api/projects/{id} | Actualizar proyecto | Sí |
| 10 | PUT | /api/projects/{id}/publish | Publicar proyecto | Sí |
| 11 | GET | /api/projects | Listar proyectos publicados | No |
| 12 | GET | /api/projects/my | Mis proyectos | Sí |
| 13 | GET | /api/projects/{id} | Ver proyecto | No |
| 14 | POST | /api/projects/{id}/apply | Aplicar a proyecto | Sí |
| 15 | GET | /api/projects/{id}/applications | Ver solicitudes | Sí |
| 16 | PUT | /api/projects/{pid}/applications/{aid}/accept | Aceptar solicitud | Sí |
| 17 | PUT | /api/projects/{pid}/applications/{aid}/reject | Rechazar solicitud | Sí |
| 18 | PUT | /api/projects/{pid}/applications/{aid}/withdraw | Retirar solicitud | Sí |
| 19 | POST | /api/discussions | Crear discusión | Sí |
| 20 | GET | /api/discussions | Listar discusiones | No |
| 21 | GET | /api/discussions/{id} | Ver discusión | No |
| 22 | PUT | /api/discussions/{id} | Actualizar discusión | Sí |
| 23 | DELETE | /api/discussions/{id} | Eliminar discusión | Sí |
| 24 | POST | /api/discussions/{id}/comments | Crear comentario | Sí |
| 25 | GET | /api/discussions/{id}/comments | Listar comentarios | No |
| 26 | PUT | /api/comments/{id} | Actualizar comentario | Sí |
| 27 | DELETE | /api/comments/{id} | Eliminar comentario | Sí |
| 28 | POST | /api/messages | Enviar mensaje | Sí |
| 29 | GET | /api/messages/conversations | Listar conversaciones | Sí |
| 30 | GET | /api/messages/conversations/{userId} | Ver mensajes | Sí |
| 31 | GET | /api/notifications/me | Ver notificaciones | Sí |
| 32 | GET | /api/statistics | Estadísticas | No |

## Importar en Postman

1. Abrir Postman
2. Click en **Import**
3. Seleccionar `DevLink.postman_collection.json`
4. Importar también `DevLink.postman_environment.json`
5. Seleccionar el ambiente **DevLink - Local**

## Importar en Bruno

1. Abrir Bruno
2. Click en **Open Collection**
3. Seleccionar la carpeta `documentation`
4. O importar: **Import** → **Postman Collection** → seleccionar `DevLink.postman_collection.json`

## Autenticación

Los endpoints marcados con **Auth: Sí** requieren header:

```
Authorization: Bearer {{token}}
```

El token se obtiene al hacer **Register** o **Login** y se guarda automáticamente en la variable `{{token}}`.

## Paginación

Endpoints con paginación aceptan query params:

```
?page=0&size=20
```

- `page`: número de página (empieza en 0)
- `size`: elementos por página (default 20)
