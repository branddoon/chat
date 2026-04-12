package com.chat.dto;

import lombok.Data;

/**
 * Payload received from the client on the {@code personal-message} Socket.IO event.
 * <p>
 * {@code socket.on('mensaje-personal', ...)} handler.
 */
@Data
public class PersonalMessagePayload {

    /** ID of the user who sent the message. */
    private String from;

    /** ID of the intended recipient. */
    private String to;

    /** Text content of the message. */
    private String message;
}
