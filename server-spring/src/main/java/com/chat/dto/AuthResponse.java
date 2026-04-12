package com.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Standard JSON response returned by all authentication endpoints.
 *
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /** Indicates whether the request succeeded. */
    private boolean ok;

    /** Email address of the authenticated user. */
    private String email;

    /** Display name of the authenticated user. */
    private String name;

    /** MongoDB document ID of the authenticated user. */
    private String id;

    /** Signed JWT for use in subsequent authenticated requests. */
    private String token;

    /** Human-readable message; present on errors or token renewal. */
    private String msg;

    /**
     * Builds a successful authentication response.
     *
     * @param email the user's email address
     * @param name  the user's display name
     * @param id    the user's MongoDB document ID
     * @param token the signed JWT
     * @return a populated {@link AuthResponse} with {@code ok = true}
     */
    public static AuthResponse success(String email, String name, String id, String token) {
        return new AuthResponse(true, email, name, id, token, null);
    }

    /**
     * Builds a failed authentication response.
     *
     * @param msg the human-readable error description
     * @return an {@link AuthResponse} with {@code ok = false}
     */
    public static AuthResponse error(String msg) {
        return new AuthResponse(false, null, null, null, null, msg);
    }
}
