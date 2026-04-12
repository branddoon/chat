import React from 'react';
import { AuthProvider }   from './auth/AuthContext';
import { ChatProvider }   from './context/chat/ChatContext';
import { SocketProvider } from './context/SocketContext';
import { AppRouter }      from './router/AppRouter';

/**
 * Root application component.
 * Wraps the entire component tree with all required context providers
 * in the correct nesting order:
 *   ChatProvider  → must be outermost so AuthProvider can dispatch to it.
 *   AuthProvider  → manages authentication state.
 *   SocketProvider → manages the Socket.IO connection based on auth state.
 */
export const ChatApp = () => {
    return (
        <ChatProvider>
            <AuthProvider>
                <SocketProvider>
                    <AppRouter />
                </SocketProvider>
            </AuthProvider>
        </ChatProvider>
    );
};
