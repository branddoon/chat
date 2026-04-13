# chat-server

Backend of the real-time chat application. Exposes a REST API for authentication and message history, and a Socket.IO server for real-time messaging.

## Stack

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.4 |
| Spring Security | (included in Boot) |
| Spring Data MongoDB | (included in Boot) |
| netty-socketio | 2.0.6 |
| jjwt | 0.12.5 |
| Lombok | (optional at runtime) |

## Prerequisites

- JDK 17+
- Maven 3.8+
- MongoDB running on `localhost:27017`

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27017/chatdb   # URI of your MongoDB instance

jwt:
  key: <secret-key-minimum-256-bits>          # Change this in production

socketio:
  port: 9092                                  # Socket.IO server port

app:
  cookie:
    secure: false   # false for local HTTP development; true in production (HTTPS)
```

> **Production:** set `app.cookie.secure: true` and use a long randomly generated JWT key. Never commit `application.yml` with real secrets to the repository.

## Running

```bash
# From the server-spring/ folder
mvn spring-boot:run
```

Or build the JAR and run it directly:

```bash
mvn clean package -DskipTests
java -jar target/chat-server-1.0.0.jar
```

The REST server will be available at `http://localhost:8080`.

## REST API

All endpoints are prefixed with `/api`.

### Authentication

| Method | Route | Auth | Description |
|---|---|---|---|
| `POST` | `/api/login/new` | No | Register a new user |
| `POST` | `/api/login` | No | Log in |
| `GET` | `/api/login/renew` | Cookie | Renew an active session |
| `POST` | `/api/login/logout` | No | Log out (expires the cookie) |

**POST `/api/login/new`**
```json
// Request
{ "name": "Brandon", "email": "brandon@mail.com", "password": "secret123" }

// Response 200
{ "ok": true, "id": "...", "name": "Brandon", "email": "brandon@mail.com", "token": "..." }

// Response 400 — email already registered
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

On a successful login or registration the server sets the `access_token` cookie with the flags `HttpOnly; SameSite=Strict; Path=/; Max-Age=28800`.

### Messages

| Method | Route | Auth | Description |
|---|---|---|---|
| `GET` | `/api/messages/:uid` | Cookie | Last 30 messages with user `:uid` |

**GET `/api/messages/:uid`**
```json
// Response 200
{
  "ok": true,
  "messages": [
    {
      "id": "...",
      "from": "<sender-uid>",
      "to": "<recipient-uid>",
      "message": "Hello!",
      "createdAt": "2026-04-12T10:00:00Z",
      "updatedAt": "2026-04-12T10:00:00Z"
    }
  ]
}
```

## Socket.IO

The Socket.IO server runs in a separate Netty process on **port 9092**.

### Handshake Authentication

The cookie does not travel to a different port, so the client passes the JWT as a query param:

```
ws://localhost:9092?x-token=<jwt>
```

If the token is invalid, the server disconnects the client immediately.

### Events

| Direction | Event | Payload |
|---|---|---|
| Server → Clients | `user-list` | `UserDto[]` — full user list sorted by online status |
| Client → Server | `personal-message` | `{ from, to, message }` |
| Server → Rooms | `personal-message` | `MessageDto` — emitted to both the sender's and recipient's rooms |

## Security

- **XSS:** the JWT travels in an `HttpOnly` cookie; JavaScript cannot read it.
- **CSRF:** the cookie uses `SameSite=Strict`; the browser does not send it on cross-site requests.
- **Passwords:** stored with BCrypt (Spring Security).
- **Sessions:** stateless (JWT); Spring Security does not maintain an HTTP session.

## Project Structure

```
src/main/java/com/chat/
├── config/
│   ├── SecurityConfig.java      # Filter chain, CORS, stateless session
│   ├── SocketIOConfig.java      # netty-socketio server bean
│   └── SocketIOLifecycle.java   # Server start/stop tied to Spring context
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
│   └── JwtHelper.java           # JWT generation and validation (JJWT)
├── middleware/
│   └── JwtFilter.java           # Reads JWT from cookie or x-token header
├── models/
│   ├── User.java                # MongoDB "users" collection
│   └── Message.java             # MongoDB "messages" collection
├── repositories/
│   ├── UserRepository.java
│   └── MessageRepository.java
└── websocket/
    └── SocketHandler.java       # connect / disconnect / personal-message events
```
