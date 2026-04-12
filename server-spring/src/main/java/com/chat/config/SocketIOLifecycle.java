package com.chat.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Ties the Socket.IO server lifecycle to the Spring application context.
 * Starts the server after all other beans (including
 * {@link com.chat.websocket.SocketHandler}) are fully initialized,
 * and stops it cleanly on application shutdown.
 */
@Component
public class SocketIOLifecycle implements SmartLifecycle {

    private final SocketIOServer socketIOServer;
    private boolean running = false;

    /**
     * @param socketIOServer the configured netty-socketio server bean
     */
    public SocketIOLifecycle(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    /**
     * Starts the Socket.IO server.
     * Called by Spring after all singleton beans have been initialized.
     */
    @Override
    public void start() {
        socketIOServer.start();
        running = true;
        System.out.println("Socket.IO server running on port: "
                + socketIOServer.getConfiguration().getPort());
    }

    /**
     * Stops the Socket.IO server.
     * Called by Spring during application context shutdown.
     */
    @Override
    public void stop() {
        socketIOServer.stop();
        running = false;
    }

    /**
     * @return {@code true} if the server is currently accepting connections
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns {@link Integer#MAX_VALUE} so this lifecycle bean starts last,
     * ensuring {@link com.chat.websocket.SocketHandler} is fully wired before
     * the server begins accepting connections.
     *
     * @return the phase value
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
