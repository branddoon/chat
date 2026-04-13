# chat-server

Backend del chat en tiempo real. Expone una API REST para autenticación e historial de mensajes, y un servidor Socket.IO para mensajería en tiempo real.

## Stack

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.4 |
| Spring Security | (incluido en Boot) |
| Spring Data MongoDB | (incluido en Boot) |
| netty-socketio | 2.0.6 |
| jjwt | 0.12.5 |
| Lombok | (opcional en runtime) |

## Requisitos previos

- JDK 17+
- Maven 3.8+
- MongoDB corriendo en `localhost:27017`

## Configuración

Edita `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27017/chatdb   # URI de tu instancia MongoDB

jwt:
  key: <clave-secreta-minimo-256-bits>        # Cambia esto en producción

socketio:
  port: 9092                                  # Puerto del servidor Socket.IO

app:
  cookie:
    secure: false   # false para desarrollo HTTP local; true en producción (HTTPS)
```

> **Producción:** establece `app.cookie.secure: true` y usa una clave JWT larga generada aleatoriamente. Nunca subas `application.yml` con secretos reales al repositorio.

## Ejecutar

```bash
# Desde la carpeta server-spring/
mvn spring-boot:run
```

O genera el JAR y ejecútalo directamente:

```bash
mvn clean package -DskipTests
java -jar target/chat-server-1.0.0.jar
```

El servidor REST queda disponible en `http://localhost:8080`.

## API REST

Todos los endpoints llevan el prefijo `/api`.

### Autenticación

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/login/new` | No | Registrar nuevo usuario |
| `POST` | `/api/login` | No | Iniciar sesión |
| `GET` | `/api/login/renew` | Cookie | Renovar sesión activa |
| `POST` | `/api/login/logout` | No | Cerrar sesión (expira la cookie) |

**POST `/api/login/new`**
```json
// Request
{ "name": "Brandon", "email": "brandon@mail.com", "password": "secret123" }

// Response 200
{ "ok": true, "id": "...", "name": "Brandon", "email": "brandon@mail.com", "token": "..." }

// Response 400 — email ya registrado
{ "ok": false, "msg": "The email already exists. Please use a different email." }
```

**POST `/api/login`**
```json
// Request
{ "email": "brandon@mail.com", "password": "secret123" }

// Response 200
{ "ok": true, "id": "...", "name": "Brandon", "email": "brandon@mail.com", "token": "..." }

// Response 404 / 400
{ "ok": false, "msg": "Email or password not found." }
```

En login y registro exitosos el servidor establece la cookie `access_token` con los flags `HttpOnly; SameSite=Strict; Path=/; Max-Age=28800`.

### Mensajes

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/api/messages/:uid` | Cookie | Últimos 30 mensajes con el usuario `:uid` |

**GET `/api/messages/:uid`**
```json
// Response 200
{
  "ok": true,
  "messages": [
    {
      "id": "...",
      "from": "<uid-remitente>",
      "to": "<uid-destinatario>",
      "message": "Hola!",
      "createdAt": "2026-04-12T10:00:00Z",
      "updatedAt": "2026-04-12T10:00:00Z"
    }
  ]
}
```

## Socket.IO

El servidor Socket.IO corre en un proceso Netty separado en el **puerto 9092**.

### Autenticación del handshake

La cookie no viaja a un puerto distinto, por lo que el cliente pasa el JWT como query param:

```
ws://localhost:9092?x-token=<jwt>
```

Si el token es inválido, el servidor desconecta al cliente inmediatamente.

### Eventos

| Dirección | Evento | Payload |
|---|---|---|
| Server → Clients | `user-list` | `UserDto[]` — lista completa de usuarios ordenada por estado online |
| Client → Server | `personal-message` | `{ from, to, message }` |
| Server → Rooms | `personal-message` | `MessageDto` — emitido a las rooms del remitente y del destinatario |

## Seguridad

- **XSS:** el JWT viaja en una cookie `HttpOnly`; JavaScript no puede leerla.
- **CSRF:** la cookie usa `SameSite=Strict`; el navegador no la envía en peticiones cross-site.
- **Contraseñas:** almacenadas con BCrypt (Spring Security).
- **Sesiones:** stateless (JWT); Spring Security no mantiene sesión HTTP.

## Estructura del proyecto

```
src/main/java/com/chat/
├── config/
│   ├── SecurityConfig.java      # Cadena de filtros, CORS, sesión stateless
│   ├── SocketIOConfig.java      # Bean del servidor netty-socketio
│   └── SocketIOLifecycle.java   # Start/stop del servidor con el contexto Spring
├── controllers/
│   ├── AuthController.java      # /api/login/**
│   └── MessagesController.java  # /api/messages/**
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── MessageDto.java
│   ├── PersonalMessagePayload.java
│   └── UserDto.java
├── helpers/
│   └── JwtHelper.java           # Generación y validación de JWT (JJWT)
├── middleware/
│   └── JwtFilter.java           # Lee JWT de cookie o header x-token
├── models/
│   ├── User.java                # Colección MongoDB "users"
│   └── Message.java             # Colección MongoDB "messages"
├── repositories/
│   ├── UserRepository.java
│   └── MessageRepository.java
└── websocket/
    └── SocketHandler.java       # Eventos connect / disconnect / personal-message
```
