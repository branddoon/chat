import React, { createContext, useReducer } from 'react';
import { chatReducer } from './chatReducer';

/** React context that exposes chat state and dispatch to the component tree. */
export const ChatContext = createContext();

/** Initial state for the chat reducer. */
const initialState = {
    uid:        '',
    activeChat: null,
    users:      [],
    messages:   [],
};

/**
 * Provides chat state (users, messages, active conversation) to all
 * descendant components via ChatContext.
 *
 * @param {{ children: React.ReactNode }} props
 */
export const ChatProvider = ({ children }) => {
    const [chatState, dispatch] = useReducer(chatReducer, initialState);

    return (
        <ChatContext.Provider value={{ chatState, dispatch }}>
            {children}
        </ChatContext.Provider>
    );
};
