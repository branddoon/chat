package com.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data transfer object representing a single chat message.
 * Used in both REST API responses and Socket.IO event payloads.
 * <p>
 * {@code mensajes} and {@code sockets} controllers.
 */
@Data
@AllArgsConstructor
public class MessageDto {

    /** MongoDB document ID of the message. */
    private String uid;

    /** ID of the user who sent the message. */
    private String from;

    /** ID of the user who received the message. */
    private String to;

    /** Text content of the message. */
    private String message;

    /** UTC timestamp when the message was first persisted. */
    private String createdAt;

    /** UTC timestamp when the message was last updated. */
    private String updatedAt;
}
