import React from 'react';
import { formatDate } from '../helpers/horaMes';

/**
 * Renders a single message received from another user.
 *
 * @param {{ msg: { message: string, createdAt: string } }} props
 */
export const IncomingMessage = ({ msg }) => {
    return (
        <div className="incoming_msg">
            <div className="incoming_msg_img">
                <img
                    src="https://ptetutorials.com/images/user-profile.png"
                    alt="user avatar"
                />
            </div>
            <div className="received_msg">
                <div className="received_withd_msg">
                    <p>{msg.message}</p>
                    <span className="time_date">{formatDate(msg.createdAt)}</span>
                </div>
            </div>
        </div>
    );
};
