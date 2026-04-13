# chat-app

Frontend del chat en tiempo real. SPA construida con React 18 que se comunica con el servidor via REST y Socket.IO.

## Stack

| Tecnología | Versión |
|---|---|
| Node.js | 22+ |
| React | 18.3 |
| React Router | 6.28 |
| Socket.IO client | 4.8 |
| Create React App | 5.0 |

## Requisitos previos

- Node.js 22+
- El servidor (`server-spring`) corriendo en `localhost:8080` y `localhost:9092`

## Configuración

Crea un archivo `.env.development` en la raíz de `chat-app/` (ya incluido en el repo):

```env
REACT_APP_API_URL=http://localhost:8080/api
```

Para producción crea `.env.production` con la URL real del servidor:

```env
REACT_APP_API_URL=https://tu-servidor.com/api
```

## Ejecutar en desarrollo

```bash
cd chat-app/
npm install
npm start
```

La app queda disponible en `http://localhost:3000`.

### Desde VS Code

Abre la carpeta `chat-app/` en VS Code y presiona **F5**. La configuración en `.vscode/launch.json` levanta el servidor de desarrollo automáticamente y abre Chrome con el debugger conectado. Los breakpoints en archivos `.js` de `src/` funcionan directamente.

## Build de producción

```bash
npm run build
```

Genera la carpeta `build/` con los archivos estáticos listos para servir.

## Seguridad — manejo del JWT

El token JWT **nunca se escribe en `localStorage`**. El flujo es:

| Capa | Mecanismo |
|---|---|
| Peticiones HTTP | Cookie `HttpOnly; SameSite=Strict` establecida por el servidor; el navegador la adjunta automáticamente con `credentials: 'include'`. JavaScript no puede leerla. |
| Socket.IO | El JWT se guarda únicamente en el estado en memoria de `AuthContext` (`auth.token`) y se pasa como query param `x-token` solo en el handshake inicial. El servidor Socket.IO corre en un puerto distinto, por lo que las cookies no viajan allí. |

## Arquitectura

### Árbol de proveedores

```
<ChatProvider>        ← estado del chat (usuarios, mensajes, chat activo)
  <AuthProvider>      ← estado de autenticación (uid, nombre, token en memoria)
    <SocketProvider>  ← conexión Socket.IO reactiva al estado de auth
      <AppRouter>     ← rutas públicas / privadas
```

### Rutas

| Ruta | Acceso | Componente |
|---|---|---|
| `/auth/login` | Público | `LoginPage` |
| `/auth/register` | Público | `RegisterPage` |
| `/*` | Privado | `ChatPage` |

Las rutas privadas redirigen a `/auth` si el usuario no está autenticado. Las rutas públicas redirigen a `/` si ya lo está.

### Flujo de autenticación

1. **Carga inicial** — `AppRouter` llama a `verifyToken()`. Se hace `GET /api/login/renew` con la cookie; si es válida se restaura la sesión, si no se muestra el login.
2. **Login / Registro** — `POST /api/login` o `/api/login/new`. El servidor responde con los datos del usuario y establece la cookie. El token también se guarda en `auth.token` (solo en memoria) para el socket.
3. **Logout** — `POST /api/login/logout` para que el servidor expire la cookie. El estado local se limpia y el socket se desconecta.

### Conexión Socket.IO

`SocketProvider` observa `auth.logged`:

- Cuando pasa a `true` → llama `connectSocket()` con `auth.token` como query param.
- Cuando pasa a `false` → llama `disconnectSocket()`.

Eventos escuchados:

| Evento | Acción |
|---|---|
| `user-list` | Actualiza la lista de usuarios en `ChatContext` |
| `personal-message` | Agrega el mensaje al historial activo y hace scroll al final |

### Estado global

**`AuthContext`** — autenticación:
```js
{ uid, checking, logged, name, email, token }
```

**`ChatContext`** — chat (reducer):
```js
{ uid, activeChat, users, messages }
```

## Estructura del proyecto

```
src/
├── auth/
│   └── AuthContext.js        # Proveedor de autenticación + acciones login/register/logout
├── context/
│   ├── chat/
│   │   ├── ChatContext.js    # Proveedor del estado del chat
│   │   └── chatReducer.js    # Reducer: usersLoaded, activateChat, newMessage, loadMessages, closeSession
│   └── SocketContext.js      # Gestión del ciclo de vida de Socket.IO
├── helpers/
│   ├── fetch.js              # fetchWithToken / fetchWithoutToken (credentials: include)
│   ├── horaMes.js            # Formateo de fechas
│   └── scrollToBottom.js     # Scroll animado al último mensaje
├── hooks/
│   └── useSocket.js          # Hook de conexión Socket.IO (recibe token como parámetro)
├── pages/
│   ├── ChatPage.js
│   ├── LoginPage.js
│   └── RegisterPage.js
├── components/
│   ├── InboxPeople.js        # Panel lateral de usuarios
│   ├── SidebarChatItem.js    # Ítem individual de conversación
│   ├── SearchBox.js          # Búsqueda de usuarios
│   ├── Messages.js           # Lista de mensajes del chat activo
│   ├── IncomingMessage.js
│   ├── OutgoingMessage.js
│   └── SendMessage.js        # Input de envío
├── router/
│   ├── AppRouter.js          # Router raíz con verificación de token
│   ├── AuthRouter.js         # Rutas públicas (login / register)
│   ├── PrivateRoute.js       # Guard de rutas autenticadas
│   └── PublicRoute.js        # Guard de rutas públicas
└── types/
    └── types.js              # Constantes de action types del reducer
```
