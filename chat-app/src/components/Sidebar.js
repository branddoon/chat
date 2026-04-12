import React, { useContext } from 'react';
import { ChatContext }    from '../context/chat/ChatContext';
import { AuthContext }    from '../auth/AuthContext';
import { SidebarChatItem } from './SidebarChatItem';

/**
 * Scrollable list of all users except the currently logged-in user.
 * Each item is clickable and opens that user's conversation.
 */
export const Sidebar = () => {
    const { chatState } = useContext(ChatContext);
    const { auth }      = useContext(AuthContext);

    return (
        <div className="inbox_chat">

            {/* Render one chat item per user, excluding ourselves */}
            {chatState.users
                .filter((user) => user.uid !== auth.uid)
                .map((user) => (
                    <SidebarChatItem key={user.uid} user={user} />
                ))
            }

            {/* Extra space at the bottom so the last item isn't cut off */}
            <div className="extra_space" />

        </div>
    );
};
