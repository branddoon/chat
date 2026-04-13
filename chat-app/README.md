# chat-app

Frontend of the real-time chat application. A Single Page Application built with React 18 that communicates with the server via REST and Socket.IO.

## Stack

| Technology | Version |
|---|---|
| Node.js | 22+ |
| React | 18.3 |
| React Router | 6.28 |
| Socket.IO Client | 4.8 |
| Create React App | 5.0 |

## Prerequisites

- Node.js 22+
- The backend (`server-spring`) running on `localhost:8080` and `localhost:9092`

## Configuration

Create a `.env.development` file in the root of `chat-app/` (already included in the repo):

```env
REACT_APP_API_URL=http://localhost:8080/api
```

For production, create `.env.production` with the real server URL:

```env
REACT_APP_API_URL=https://your-server.com/api
```

## Running in Development

```bash
cd chat-app/
npm install
npm start
```

The app will be available at `http://localhost:3000`.

### From VS Code

Open the `chat-app/` folder in VS Code and press **F5**. The configuration in `.vscode/launch.json` starts the dev server automatically and opens Chrome with the debugger attached. Breakpoints in `.js` files under `src/` work out of the box.

## Production Build

```bash
npm run build
```

Generates the `build/` folder with static files ready to be served.

## Security — JWT Handling

The JWT token is **never written to `localStorage`**. The flow is:

| Layer | Mechanism |
|---|---|
| HTTP requests | `HttpOnly; SameSite=Strict` cookie set by the server; the browser attaches it automatically with `credentials: 'include'`. JavaScript cannot read it. |
| Socket.IO | The JWT is stored only in the in-memory state of `AuthContext` (`auth.token`) and passed as the `x-token` query param solely during the initial handshake. The Socket.IO server runs on a different port, so cookies do not travel there. |

## Architecture

### Provider Tree

```
<ChatProvider>        ← chat state (users, messages, active chat)
  <AuthProvider>      ← auth state (uid, name, in-memory token)
    <SocketProvider>  ← Socket.IO connection reactive to auth state
      <AppRouter>     ← public / private routes
```

### Routes

| Route | Access | Component |
|---|---|---|
| `/auth/login` | Public | `LoginPage` |
| `/auth/register` | Public | `RegisterPage` |
| `/*` | Private | `ChatPage` |

Private routes redirect to `/auth` if the user is not authenticated. Public routes redirect to `/` if they already are.

### Authentication Flow

1. **Initial load** — `AppRouter` calls `verifyToken()`. It sends `GET /api/login/renew` with the cookie; if valid the session is restored, otherwise the login screen is shown.
2. **Login / Register** — `POST /api/login` or `/api/login/new`. The server responds with the user data and sets the cookie. The token is also stored in `auth.token` (in memory only) for the socket handshake.
3. **Logout** — `POST /api/login/logout` so the server expires the cookie. Local state is cleared and the socket disconnects.

### Socket.IO Connection

`SocketProvider` watches `auth.logged`:

- When it becomes `true` → calls `connectSocket()` with `auth.token` as the query param.
- When it becomes `false` → calls `disconnectSocket()`.

Events listened to:

| Event | Action |
|---|---|
| `user-list` | Updates the user list in `ChatContext` |
| `personal-message` | Appends the message to the active history and scrolls to the bottom |

### Global State

**`AuthContext`** — authentication:
```js
{ uid, checking, logged, name, email, token }
```

**`ChatContext`** — chat (reducer):
```js
{ uid, activeChat, users, messages }
```

## Project Structure

```
src/
├── auth/
│   └── AuthContext.js        # Auth provider + login/register/logout actions
├── context/
│   ├── chat/
│   │   ├── ChatContext.js    # Chat state provider
│   │   └── chatReducer.js    # Reducer: usersLoaded, activateChat, newMessage, loadMessages, closeSession
│   └── SocketContext.js      # Socket.IO lifecycle management
├── helpers/
│   ├── fetch.js              # fetchWithToken / fetchWithoutToken (credentials: include)
│   ├── horaMes.js            # Date formatting
│   └── scrollToBottom.js     # Animated scroll to the last message
├── hooks/
│   └── useSocket.js          # Socket.IO connection hook (receives token as parameter)
├── pages/
│   ├── ChatPage.js
│   ├── LoginPage.js
│   └── RegisterPage.js
├── components/
│   ├── InboxPeople.js        # User list panel
│   ├── SidebarChatItem.js    # Individual conversation item
│   ├── SearchBox.js          # User search
│   ├── Messages.js           # Message list for the active chat
│   ├── IncomingMessage.js
│   ├── OutgoingMessage.js
│   └── SendMessage.js        # Message input
├── router/
│   ├── AppRouter.js          # Root router with token verification
│   ├── AuthRouter.js         # Public routes (login / register)
│   ├── PrivateRoute.js       # Authenticated route guard
│   └── PublicRoute.js        # Public route guard
└── types/
    └── types.js              # Reducer action type constants
```
