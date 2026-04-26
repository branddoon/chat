package com.chat.websocket;

import com.chat.dto.MessageDto;
import com.chat.dto.PersonalMessagePayload;
import com.chat.dto.UserDto;
import com.chat.helpers.JwtHelper;
import com.chat.models.Message;
import com.chat.repositories.MessageRepository;
import com.chat.repositories.UserRepository;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Socket.IO event handler that manages real-time messaging and user presence.
 *
 * <p>Handled events:</p>
 * <ul>
 *   <li><b>connect</b>           – validates JWT, marks user online, joins private room,
 *                                  broadcasts {@code user-list}</li>
 *   <li><b>personal-message</b>  – persists the message and emits it to both participants</li>
 *   <li><b>disconnect</b>        – marks user offline, broadcasts updated {@code user-list}</li>
 * </ul>
 */
@Component
public class SocketHandler {

    private final SocketIOServer server;
    private final JwtHelper jwtHelper;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    /**
     * Registers all Socket.IO event listeners at construction time, before the
     * server starts (see {@link com.chat.config.SocketIOLifecycle}).
     *
     * @param server            the netty-socketio server instance
     * @param jwtHelper         JWT validation utility
     * @param userRepository    data access for {@link com.chat.models.User} documents
     * @param messageRepository data access for {@link Message} documents
     */
    public SocketHandler(SocketIOServer server,
                         JwtHelper jwtHelper,
                         UserRepository userRepository,
                         MessageRepository messageRepository) {
        this.server            = server;
        this.jwtHelper         = jwtHelper;
        this.userRepository    = userRepository;
        this.messageRepository = messageRepository;

        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);
        server.addEventListener("personal-message", PersonalMessagePayload.class, this::onPersonalMessage);
    }

    /**
     * Handles a new socket connection.
     * Reads the JWT from the {@code access_token} HttpOnly cookie in the handshake
     * headers, validates it, marks the user online, joins the user's private room
     * (named after their UID), and broadcasts the updated user list to all clients.
     *
     * @param client the newly connected socket client
     */
    private void onConnect(SocketIOClient client) {
        String token    = resolveToken(client);
        String[] result = jwtHelper.verifyJWT(token);

        if (!"true".equals(result[0])) {
            System.out.println("Socket not identified — disconnecting client.");
            client.disconnect();
            return;
        }

        String uid = result[1];
        client.set("uid", uid);

        userConnected(uid);
        client.joinRoom(uid);

        server.getBroadcastOperations().sendEvent("user-list", getUsers());
    }

    /**
     * Handles a socket disconnection.
     * Marks the user offline and broadcasts the updated user list.
     *
     * @param client the disconnected socket client
     */
    private void onDisconnect(SocketIOClient client) {
        String uid = client.get("uid");
        if (uid != null) {
            userDisconnected(uid);
            server.getBroadcastOperations().sendEvent("user-list", getUsers());
        }
    }

    /**
     * Handles an incoming {@code personal-message} socket event.
     * Persists the message to MongoDB and emits a {@code personal-message} event
     * to both the sender's and recipient's private rooms.
     *
     * @param client     the socket client that emitted the event
     * @param payload    the message payload ({@code from}, {@code to}, {@code message})
     * @param ackRequest Socket.IO acknowledgement callback (unused)
     */
    private void onPersonalMessage(SocketIOClient client,
                                   PersonalMessagePayload payload,
                                   AckRequest ackRequest) {
        Message message = new Message();
        message.setFrom(payload.getFrom());
        message.setTo(payload.getTo());
        message.setMessage(payload.getMessage());
        messageRepository.save(message);

        MessageDto dto = new MessageDto(
                message.getId(),
                message.getFrom(),
                message.getTo(),
                message.getMessage(),
                message.getCreatedAt().toString(),
                message.getUpdatedAt().toString());

        server.getRoomOperations(payload.getTo()).sendEvent("personal-message", dto);
        server.getRoomOperations(payload.getFrom()).sendEvent("personal-message", dto);
    }

    /**
     * Extracts the JWT from the {@code access_token} HttpOnly cookie sent in the
     * WebSocket handshake request headers.
     *
     * @param client the connecting socket client
     * @return the raw JWT string, or {@code null} if the cookie is absent
     */
    private String resolveToken(SocketIOClient client) {
        String cookieHeader = client.getHandshakeData().getHttpHeaders().get("Cookie");
        if (cookieHeader == null) return null;
        return ServerCookieDecoder.STRICT.decode(cookieHeader).stream()
                .filter(c -> "access_token".equals(c.name()))
                .map(io.netty.handler.codec.http.cookie.Cookie::value)
                .findFirst()
                .orElse(null);
    }

    /**
     * Marks the user with the given ID as online in the database.
     *
     * @param uid the user's MongoDB document ID
     */
    private void userConnected(String uid) {
        userRepository.findById(uid).ifPresent(u -> {
            u.setOnline(true);
            userRepository.save(u);
        });
    }

    /**
     * Marks the user with the given ID as offline in the database.
     *
     * @param uid the user's MongoDB document ID
     */
    private void userDisconnected(String uid) {
        userRepository.findById(uid).ifPresent(u -> {
            u.setOnline(false);
            userRepository.save(u);
        });
    }

    /**
     * Retrieves all users sorted by online status (online users first).
     *
     * @return list of {@link UserDto} objects sorted descending by {@code online}
     */
    private List<UserDto> getUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "online"))
                .stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.isOnline()))
                .collect(Collectors.toList());
    }
}
