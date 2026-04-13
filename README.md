# Chat App

A real-time chat application with a client-server architecture. The frontend is built with React 18 and the backend with Spring Boot 3, communicating over a REST API and Socket.IO.

---

## Table of Contents

- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Installation & Running](#installation--running)
- [REST API](#rest-api)
- [Socket.IO Events](#socketio-events)
- [Security](#security)
- [Project Structure](#project-structure)

---

## Architecture

```
┌─────────────────────┐        REST (HTTP :8080)        ┌──────────────────────────┐
│                     │ ──────────────────────────────► │                          │
│   React 18 (SPA)    │                                  │  Spring Boot 3.2 Server  │
│   chat-app/         │ ◄────────────────────────────── │  server-spring/          │
│                     │        Socket.IO (:9092)         │                          │
└─────────────────────┘ ◄────────────────────────────── └──────────┬───────────────┘
                                                                    │
                                                                    ▼
                                                             MongoDB (chatdb)
```

- **Port 8080** — REST API (auth + message history)
- **Port 9092** — Socket.IO server (real-time messaging and user presence)

---

## Technologies

### Frontend (`chat-app/`)

| Technology | Version | Purpose |
|---|---|---|
| React | 18.3 | UI / components |
| React Router DOM | 6.28 | SPA routing |
| Socket.IO Client | 4.8 | Real-time messaging |
| Moment.js | 2.30 | Date formatting |
| SweetAlert2 | 11 | Alerts and notifications |
| Node.js | ≥ 22 | Development environment |

### Backend (`server-spring/`)

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.2.4 | Main framework |
| Java | 17 | Language |
| Spring Security | — | Authentication and authorization |
| Spring Data MongoDB | — | Data access |
| netty-socketio | 2.0.6 | Socket.IO server |
| JJWT | 0.12.5 | JWT generation and validation |
| Lombok | — | Boilerplate reduction |
| Maven | — | Dependency management |

### Database

| Technology | Purpose |
|---|---|
| MongoDB | User and message storage |

---

## Prerequisites

- **Node.js** v22 or higher
- **Java** 17 or higher
- **Maven** 3.8 or higher
- **MongoDB** running locally on `localhost:27017`

---

## Configuration

### Backend — `server-spring/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27017/chatdb

jwt:
  key: <long-secret-key>

socketio:
  port: 9092

app:
  cookie:
    # false for local HTTP development; true in production (requires HTTPS)
    secure: false
```

> **Important:** Replace `jwt.key` with a secure random key before deploying to production and set `app.cookie.secure: true`.

### Frontend — `chat-app/.env.development`

```env
REACT_APP_API_URL=http://localhost:8080/api
```

The Socket.IO client connects to `http://localhost:9092` (configured in `SocketContext.js`).

---

## Installation & Running

### 1. Database

Make sure MongoDB is running locally:

```bash
mongod --dbpath /path/to/your/data
```

The `chatdb` database is created automatically on the first startup.

### 2. Backend (Spring Boot)

```bash
cd server-spring
mvn spring-boot:run
```

The REST server starts at `http://localhost:8080` and the Socket.IO server at port `9092`.

### 3. Frontend (React)

```bash
cd chat-app
npm install
npm start
```

The app opens at `http://localhost:3000`.

### Production Build (Frontend)

```bash
cd chat-app
npm run build
```

Static files are generated in `chat-app/build/`.

---

## REST API

Base URL: `http://localhost:8080/api`

All protected endpoints require the `access_token` HttpOnly cookie, which the browser sends automatically.

### Authentication

#### Register a user

```
POST /login/new
```

**Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Success `200`:**
```json
{
  "ok": true,
  "email": "john@example.com",
  "name": "John Doe",
  "id": "<mongodb-id>",
  "token": "<jwt>"
}
```

**Error `400`** — Email already in use.

---

#### Log in

```
POST /login
```

**Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Success `200`:** same as registration.

**Error `404`** — Email not found.  
**Error `400`** — Wrong password.

---

#### Renew token

```
GET /login/renew
```

Requires a valid `access_token` cookie. Returns a renewed token and refreshes the cookie.

---

#### Log out

```
POST /login/logout
```

Expires the `access_token` cookie server-side. No request body required.

---

### Messages

#### Conversation history

```
GET /messages/:uid
```

Requires a valid `access_token` cookie.

Returns the last 30 messages between the authenticated user and the user identified by `:uid`, sorted oldest-first.

**Response `200`:**
```json
{
  "ok": true,
  "messages": [
    {
      "id": "<id>",
      "from": "<sender-uid>",
      "to": "<recipient-uid>",
      "message": "Hello!",
      "createdAt": "2026-04-12T10:00:00Z",
      "updatedAt": "2026-04-12T10:00:00Z"
    }
  ]
}
```

---

## Socket.IO Events

The Socket.IO server listens on port `9092`. Authentication is done by passing the JWT as the `x-token` query parameter during the initial handshake.

### Connecting

```js
io('http://localhost:9092', { query: { 'x-token': '<jwt>' } })
```

If the token is invalid, the server disconnects the client immediately.

---

### Events emitted by the server

| Event | Description | Payload |
|---|---|---|
| `user-list` | Full list of users (online first). Broadcast to all clients on any connect/disconnect. | `[{ id, name, email, online }]` |
| `personal-message` | Private message. Emitted to both sender and recipient. | `{ id, from, to, message, createdAt, updatedAt }` |

---

### Events listened to by the server

| Event | Description | Payload |
|---|---|---|
| `personal-message` | Send a private message to another user. | `{ from, to, message }` |

---

## Security

### JWT via HttpOnly Cookie

- After login or registration, the server sets an `access_token` cookie with the `HttpOnly`, `SameSite=Strict`, and (in production) `Secure` flags.
- Client-side JavaScript **cannot read** the token, eliminating the XSS token-theft attack vector.
- Authenticated HTTP requests send the cookie automatically — no manual `Authorization` header needed.

### Socket.IO — In-memory token for handshake

- Because the Socket.IO server runs on a different port (9092), cookies are not included in the WebSocket handshake.
- To solve this, the token is kept **in memory only** (React state) after login and passed as `x-token` solely during the initial socket connection.
- The token is **never stored** in `localStorage` or `sessionStorage`.

### Passwords

- Passwords are stored as **BCrypt** hashes via Spring Security.

---

## Project Structure

```
chat/
├── chat-app/                        # React 18 Frontend
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── auth/
│   │   │   └── AuthContext.js       # Auth state and actions
│   │   ├── components/
│   │   │   ├── ChatSelect.js        # Empty screen when no chat is selected
│   │   │   ├── InboxPeople.js       # User list in the sidebar
│   │   │   ├── IncomingMessage.js   # Received message bubble
│   │   │   ├── Messages.js          # Message history container
│   │   │   ├── OutgoingMessage.js   # Sent message bubble
│   │   │   ├── SearchBox.js         # User search box
│   │   │   ├── SendMessage.js       # Message input and send button
│   │   │   ├── Sidebar.js           # Sidebar layout
│   │   │   └── SidebarChatItem.js   # Conversation item in the sidebar
│   │   ├── context/
│   │   │   ├── chat/
│   │   │   │   └── ChatContext.js   # Global chat state (reducer)
│   │   │   └── SocketContext.js     # Socket.IO connection and events
│   │   ├── css/
│   │   │   ├── chat.css
│   │   │   └── login-register.css
│   │   ├── helpers/
│   │   │   ├── fetch.js             # HTTP helpers (with/without token)
│   │   │   ├── horaMes.js           # Date formatting with Moment.js
│   │   │   └── scrollToBottom.js   # Auto-scroll for message list
│   │   ├── hooks/
│   │   │   └── useSocket.js         # Hook to manage the socket lifecycle
│   │   ├── pages/
│   │   │   ├── ChatPage.js          # Main chat page
│   │   │   ├── LoginPage.js         # Login page
│   │   │   └── RegisterPage.js      # Registration page
│   │   ├── router/
│   │   │   ├── AppRouter.js         # Root router with token verification
│   │   │   ├── AuthRouter.js        # Public routes (login / register)
│   │   │   ├── PrivateRoute.js      # Guard for authenticated routes
│   │   │   └── PublicRoute.js       # Guard for public routes
│   │   ├── types/
│   │   │   └── types.js             # Reducer action constants
│   │   ├── ChatApp.js               # Root component with context providers
│   │   └── index.js                 # Entry point
│   ├── .env.development
│   └── package.json
│
└── server-spring/                   # Spring Boot 3 Backend
    └── src/main/java/com/chat/
        ├── config/
        │   ├── MongoConfig.java      # MongoDB configuration
        │   ├── SecurityConfig.java   # Spring Security configuration
        │   └── SocketIOConfig.java   # Socket.IO server bean
        ├── controllers/
        │   ├── AuthController.java   # Authentication endpoints
        │   └── MessagesController.java # Message history endpoint
        ├── dto/                      # Data Transfer Objects
        ├── helpers/
        │   └── JwtHelper.java        # JWT generation and validation
        ├── middleware/
        │   └── JwtFilter.java        # Spring Security JWT filter
        ├── models/
        │   ├── User.java             # MongoDB user document
        │   └── Message.java          # MongoDB message document
        ├── repositories/             # Spring Data MongoDB repositories
        ├── websocket/
        │   ├── SocketHandler.java    # Socket.IO event handler
        │   └── SocketIOLifecycle.java # Socket.IO server lifecycle
        └── ChatApplication.java      # Spring Boot entry point
```
