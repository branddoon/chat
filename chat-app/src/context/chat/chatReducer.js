import { types } from '../../types/types';

/**
 * Pure reducer function for the chat state.
 * Handles user list updates, active chat selection,
 * incoming messages, loading chat history, and session cleanup.
 *
 * @param {object} state  - Current chat state.
 * @param {object} action - Dispatched action with type and optional payload.
 * @returns {object} Next chat state.
 */
export const chatReducer = (state, action) => {
    switch (action.type) {

        // Replace the full user list with the latest snapshot from the server.
        case types.usersLoaded:
            return {
                ...state,
                users: [...action.payload],
            };

        // Set the active conversation target; skip update if already active.
        case types.activateChat:
            if (state.activeChat === action.payload) return state;
            return {
                ...state,
                activeChat: action.payload,
            };

        // Append an incoming message only if it belongs to the active conversation.
        case types.newMessage:
            if (
                state.activeChat === action.payload.from ||
                state.activeChat === action.payload.to
            ) {
                return {
                    ...state,
                    messages: [...state.messages, action.payload],
                };
            }
            return state;

        // Replace the message list with a freshly fetched history.
        case types.loadMessages:
            return {
                ...state,
                messages: [...action.payload],
            };

        // Reset all chat state on logout.
        case types.closeSession:
            return {
                uid:        '',
                activeChat: null,
                users:      [],
                messages:   [],
            };

        default:
            return state;
    }
};
