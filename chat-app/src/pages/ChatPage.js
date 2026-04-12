import React, { useContext } from 'react';
import { ChatContext }  from '../context/chat/ChatContext';
import { ChatSelect }   from '../components/ChatSelect';
import { InboxPeople }  from '../components/InboxPeople';
import { Messages }     from '../components/Messages';
import '../css/chat.css';

/**
 * Main chat page layout.
 * Shows the contact sidebar on the left and either a prompt to select
 * a conversation or the active message thread on the right.
 */
export const ChatPage = () => {
    const { chatState } = useContext(ChatContext);

    return (
        <div className="messaging">
            <div className="inbox_msg">

                {/* Left panel: user list and search */}
                <InboxPeople />

                {/* Right panel: active conversation or placeholder */}
                {chatState.activeChat
                    ? <Messages />
                    : <ChatSelect />
                }

            </div>
        </div>
    );
};
