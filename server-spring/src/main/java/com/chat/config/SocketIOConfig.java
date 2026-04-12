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

    /**
     * Creates and configures the {@link SocketIOServer} bean.
     * CORS is set to {@code *} to accept connections from any origin.
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
        config.setOrigin("*");
        return new SocketIOServer(config);
    }
}
