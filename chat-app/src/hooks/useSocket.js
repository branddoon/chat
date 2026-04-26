import { useCallback, useEffect, useState } from 'react';
import { io } from 'socket.io-client';

/**
 * Custom hook that manages a Socket.IO connection lifecycle.
 *
 * Authentication is handled automatically by the browser: the {@code access_token}
 * HttpOnly cookie is attached to the WebSocket handshake request because the socket
 * server (localhost:9092) is same-site with the React app (localhost:3000).
 * {@code withCredentials: true} is required so that the socket CORS response header
 * reflects the exact origin instead of {@code *}.
 *
 * @param {string} serverPath - URL of the Socket.IO server.
 * @returns {{ socket, online, connectSocket, disconnectSocket }}
 */
export const useSocket = (serverPath) => {
    const [socket, setSocket]   = useState(null);
    const [online, setOnline]   = useState(false);

    /** Opens a new socket connection; the HttpOnly cookie authenticates the handshake. */
    const connectSocket = useCallback(() => {
        const newSocket  = io(serverPath, {
            transports:     ['websocket'],
            autoConnect:    true,
            forceNew:       true,
            withCredentials: true,
        });
        setSocket(newSocket);
    }, [serverPath]);

    /** Closes the current socket connection if one is open. */
    const disconnectSocket = useCallback(() => {
        socket?.disconnect();
    }, [socket]);

    // Sync online status whenever the socket instance changes.
    useEffect(() => {
        setOnline(socket?.connected ?? false);
    }, [socket]);

    // Update online status on connect event.
    useEffect(() => {
        socket?.on('connect', () => setOnline(true));
        return () => { socket?.off('connect'); };
    }, [socket]);

    // Update online status on disconnect event.
    useEffect(() => {
        socket?.on('disconnect', () => setOnline(false));
        return () => { socket?.off('disconnect'); };
    }, [socket]);

    return { socket, online, connectSocket, disconnectSocket };
};
