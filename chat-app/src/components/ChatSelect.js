import React from 'react';

/**
 * Placeholder shown in the right panel when no conversation is selected.
 * Prompts the user to pick a contact from the sidebar.
 */
export const ChatSelect = () => {
    return (
        <div className="middle-screen">
            <div className="alert-info">
                <hr />
                <h3>Select a person</h3>
                <span>To start a conversation</span>
            </div>
        </div>
    );
};
