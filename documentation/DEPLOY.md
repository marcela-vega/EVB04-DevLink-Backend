# DevLink Backend - Guía de despliegue

## Requisitos

- Docker instalado
- Acceso a una base de datos PostgreSQL (se usa Supabase)

## Configuración

1. Copia el archivo de variables de entorno:

```bash
cp .env.example .env
```

2. Edita `.env` con tus valores:

```env
# Supabase
DB_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=tu_password_de_supabase

# JWT (mínimo 32 caracteres)
JWT_SECRET=tu_jwt_secret_aqui_minimo_32_caracteres
```

Las variables opcionales (`JPA_DDL_AUTO`, `SERVER_PORT`, `JWT_EXPIRATION_MS`) tienen valores por defecto y no son necesarias salvo que quieras cambiarlos.

## Construir la imagen

```bash
docker build --network=host -t devlink-backend .
```

> El flag `--network=host` es necesario para que el build pueda descargar dependencias de Maven.

## Levantar el contenedor

```bash
docker compose up -d
```

La API queda disponible en `http://localhost:8080`.

## Verificar que está corriendo

```bash
docker compose logs -f app
```

O hacer un health check:

```bash
curl http://localhost:8080/api/health
```

## Detener

```bash
docker compose down
```
