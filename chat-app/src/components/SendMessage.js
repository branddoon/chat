import React, { useState, useContext } from 'react';
import { AuthContext }   from '../auth/AuthContext';
import { ChatContext }   from '../context/chat/ChatContext';
import { SocketContext } from '../context/SocketContext';

/**
 * Message input form at the bottom of the chat panel.
 * Emits a `personal-message` socket event on submission and clears the input.
 */
export const SendMessage = () => {
    const [message, setMessage]  = useState('');
    const { socket }             = useContext(SocketContext);
    const { auth }               = useContext(AuthContext);
    const { chatState }          = useContext(ChatContext);

    /** Keeps the controlled input in sync with component state. */
    const onChange = ({ target }) => {
        setMessage(target.value);
    };

    /** Emits the message to the server and resets the input field. */
    const onSubmit = (e) => {
        e.preventDefault();

        // Do nothing if the input is blank.
        if (message.trim().length === 0) return;

        socket.emit('personal-message', {
            from:    auth.uid,
            to:      chatState.activeChat,
            message,
        });

        setMessage('');
    };

    return (
        <form onSubmit={onSubmit}>
            <div className="type_msg row">
                {/* Text input */}
                <div className="input_msg_write col-sm-9">
                    <input
                        type="text"
                        className="write_msg"
                        placeholder="Message..."
                        value={message}
                        onChange={onChange}
                    />
                </div>

                {/* Send button */}
                <div className="col-sm-3 text-center">
                    <button
                        className="msg_send_btn mt-3"
                        type="submit"
                    >
                        Send
                    </button>
                </div>
            </div>
        </form>
    );
};
