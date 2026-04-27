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
| 11 | GET | /api/projects | Listar proyectos | No |
| 12 | GET | /api/projects/{id} | Ver proyecto | No |
| 13 | POST | /api/projects/{id}/apply | Aplicar a proyecto | Sí |
| 14 | GET | /api/projects/{id}/applications | Ver solicitudes | Sí |
| 15 | PUT | /api/projects/{pid}/applications/{aid}/accept | Aceptar solicitud | Sí |
| 16 | PUT | /api/projects/{pid}/applications/{aid}/reject | Rechazar solicitud | Sí |
| 17 | PUT | /api/projects/{pid}/applications/{aid}/withdraw | Retirar solicitud | Sí |
| 18 | POST | /api/discussions | Crear discusión | Sí |
| 19 | GET | /api/discussions | Listar discusiones | No |
| 20 | GET | /api/discussions/{id} | Ver discusión | No |
| 21 | PUT | /api/discussions/{id} | Actualizar discusión | Sí |
| 22 | DELETE | /api/discussions/{id} | Eliminar discusión | Sí |
| 23 | POST | /api/discussions/{id}/comments | Crear comentario | Sí |
| 24 | GET | /api/discussions/{id}/comments | Listar comentarios | No |
| 25 | PUT | /api/comments/{id} | Actualizar comentario | Sí |
| 26 | DELETE | /api/comments/{id} | Eliminar comentario | Sí |
| 27 | POST | /api/messages | Enviar mensaje | Sí |
| 28 | GET | /api/messages/conversations | Listar conversaciones | Sí |
| 29 | GET | /api/messages/conversations/{userId} | Ver mensajes | Sí |
| 30 | GET | /api/notifications/me | Ver notificaciones | Sí |
| 31 | GET | /api/statistics | Estadísticas | No |

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
