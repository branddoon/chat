package com.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data transfer object for a user entry in the contact list.
 * Emitted to all clients on the {@code user-list} Socket.IO event.
 */
@Data
@AllArgsConstructor
public class UserDto {

    /** MongoDB document ID of the user. */
    private String uid;

    /** Display name of the user. */
    private String name;

    /** Email address of the user. */
    private String email;

    /** Whether the user currently has an active socket connection. */
    private boolean online;
}
