import React from 'react';
import { formatDate } from '../helpers/horaMes';

/**
 * Renders a single message sent by the current user.
 *
 * @param {{ msg: { message: string, createdAt: string } }} props
 */
export const OutgoingMessage = ({ msg }) => {
    return (
        <div className="outgoing_msg">
            <div className="sent_msg">
                <p>{msg.message}</p>
                <span className="time_date">{formatDate(msg.createdAt)}</span>
            </div>
        </div>
    );
};
