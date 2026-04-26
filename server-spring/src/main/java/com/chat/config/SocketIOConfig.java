package com.chat.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the netty-socketio server bean.
 *
 * <p><b>Note:</b> netty-socketio runs on a separate port ({@code socketio.port=9092}).
 * The frontend must connect to that port rather than the REST API port (8080).</p>
 */
@Configuration
public class SocketIOConfig {

    @Value("${socketio.port:9092}")
    private int socketioPort;

    @Value("${app.cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;

    /**
     * Creates and configures the {@link SocketIOServer} bean.
     * The allowed origin must be explicit (not {@code *}) so that the browser
     * sends the {@code access_token} HttpOnly cookie during the WebSocket handshake.
     * The server is not started here; lifecycle management is handled by
     * {@link SocketIOLifecycle}.
     *
     * @return the configured (but not yet started) {@link SocketIOServer}
     */
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setPort(socketioPort);
        config.setOrigin(allowedOrigin);
        return new SocketIOServer(config);
    }
}
