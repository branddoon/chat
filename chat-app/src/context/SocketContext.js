import React, { useEffect, useContext, createContext } from 'react';
import { useSocket } from '../hooks/useSocket';
import { AuthContext } from '../auth/AuthContext';
import { ChatContext } from './chat/ChatContext';
import { types } from '../types/types';
import { scrollToBottomAnimated } from '../helpers/scrollToBottom';

/** React context that exposes the socket instance and online status. */
export const SocketContext = createContext();

/**
 * Manages the Socket.IO connection in response to auth state changes and
 * forwards server-emitted events into the chat reducer.
 *
 * @param {{ children: React.ReactNode }} props
 */
export const SocketProvider = ({ children }) => {
    const { socket, online, connectSocket, disconnectSocket } = useSocket('http://localhost:9092');
    const { auth }             = useContext(AuthContext);
    const { dispatch }         = useContext(ChatContext);

    // Connect the socket when the user logs in.
    useEffect(() => {
        if (auth.logged) connectSocket();
    }, [auth, connectSocket]);

    // Disconnect the socket when the user logs out.
    useEffect(() => {
        if (!auth.logged) disconnectSocket();
    }, [auth, disconnectSocket]);

    // Listen for updated user lists broadcast by the server.
    useEffect(() => {
        socket?.on('user-list', (users) => {
            dispatch({ type: types.usersLoaded, payload: users });
        });
        return () => { socket?.off('user-list'); };
    }, [socket, dispatch]);

    // Listen for personal messages directed at the current user.
    useEffect(() => {
        socket?.on('personal-message', (message) => {
            dispatch({ type: types.newMessage, payload: message });
            scrollToBottomAnimated('messages');
        });
        return () => { socket?.off('personal-message'); };
    }, [socket, dispatch]);

    return (
        <SocketContext.Provider value={{ socket, online }}>
            {children}
        </SocketContext.Provider>
    );
};
