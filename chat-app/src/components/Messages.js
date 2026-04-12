import React, { useContext } from 'react';
import { AuthContext }       from '../auth/AuthContext';
import { ChatContext }       from '../context/chat/ChatContext';
import { IncomingMessage }   from './IncomingMessage';
import { OutgoingMessage }   from './OutgoingMessage';
import { SendMessage }       from './SendMessage';

/**
 * Right panel that displays the active conversation's message history
 * and the input form for sending new messages.
 * Messages whose `to` field matches the current user's uid are rendered
 * as incoming; all others are rendered as outgoing.
 */
export const Messages = () => {
    const { chatState } = useContext(ChatContext);
    const { auth }      = useContext(AuthContext);

    return (
        <div className="mesgs">

            {/* Scrollable message history */}
            <div id="messages" className="msg_history">
                {chatState.messages.map((msg) =>
                    msg.to === auth.uid
                        ? <IncomingMessage key={msg.uid} msg={msg} />
                        : <OutgoingMessage key={msg.uid} msg={msg} />
                )}
            </div>

            {/* Message input form */}
            <SendMessage />

        </div>
    );
};
