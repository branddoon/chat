import { useCallback, useEffect, useState } from 'react';
import { io } from 'socket.io-client';

/**
 * Custom hook that manages a Socket.IO connection lifecycle.
 *
 * The token is received as a parameter (from AuthContext in-memory state) rather
 * than being read from localStorage. This avoids the XSS token-theft vector while
 * still allowing the socket server — which runs on a different port and therefore
 * cannot receive cookies automatically — to authenticate the handshake.
 *
 * @param {string} serverPath - URL of the Socket.IO server.
 * @param {string|null} token - JWT for the socket handshake, held only in memory.
 * @returns {{ socket, online, connectSocket, disconnectSocket }}
 */
export const useSocket = (serverPath, token) => {
    const [socket, setSocket]   = useState(null);
    const [online, setOnline]   = useState(false);

    /** Opens a new socket connection authenticated with the in-memory token. */
    const connectSocket = useCallback(() => {
        const newSocket  = io(serverPath, {
            transports: ['websocket'],
            autoConnect: true,
            forceNew:    true,
            query: { 'x-token': token },
        });
        setSocket(newSocket);
    }, [serverPath, token]);

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
