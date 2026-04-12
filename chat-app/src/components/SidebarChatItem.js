import React, { useContext } from 'react';
import { ChatContext }    from '../context/chat/ChatContext';
import { types }          from '../types/types';
import { fetchWithToken } from '../helpers/fetch';
import { scrollToBottom } from '../helpers/scrollToBottom';

/**
 * A single clickable row in the contact sidebar.
 * On click: activates the conversation, fetches the last 30 messages
 * from the API, and scrolls the message list to the bottom.
 *
 * @param {{ user: { uid: string, name: string, online: boolean } }} props
 */
export const SidebarChatItem = ({ user }) => {
    const { chatState, dispatch } = useContext(ChatContext);
    const { activeChat }          = chatState;

    const onClick = async () => {
        // Mark this user's conversation as active in global state.
        dispatch({ type: types.activateChat, payload: user.uid });

        // Fetch message history for this conversation.
        const resp = await fetchWithToken(`messages/${user.uid}`);
        dispatch({ type: types.loadMessages, payload: resp.messages });

        // Scroll the message container to the latest message.
        scrollToBottom('messages');
    };

    return (
        <div
            className={`chat_list ${user.uid === activeChat ? 'active_chat' : ''}`}
            onClick={onClick}
        >
            <div className="chat_people">
                {/* User avatar */}
                <div className="chat_img">
                    <img
                        src="https://ptetutorials.com/images/user-profile.png"
                        alt="user avatar"
                    />
                </div>

                {/* User info */}
                <div className="chat_ib">
                    <h5>{user.name}</h5>
                    {user.online
                        ? <span className="text-success">Online</span>
                        : <span className="text-danger">Offline</span>
                    }
                </div>
            </div>
        </div>
    );
};
