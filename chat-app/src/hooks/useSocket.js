import { useCallback, useEffect, useState } from 'react';
import { io } from 'socket.io-client';

/**
 * Custom hook that manages a Socket.IO connection lifecycle.
 * Reads the JWT from localStorage and attaches it as a query param
 * so the server can authenticate the socket handshake.
 *
 * @param {string} serverPath - URL of the Socket.IO server.
 * @returns {{ socket, online, connectSocket, disconnectSocket }}
 */
export const useSocket = (serverPath) => {
    const [socket, setSocket]   = useState(null);
    const [online, setOnline]   = useState(false);

    /** Opens a new socket connection authenticated with the stored token. */
    const connectSocket = useCallback(() => {
        const token      = localStorage.getItem('token');
        const newSocket  = io(serverPath, {
            transports: ['websocket'],
            autoConnect: true,
            forceNew:    true,
            query: { 'x-token': token },
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
