package com.chat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/login/new}.
 *
 */
@Data
public class RegisterRequest {

    /** Display name of the new user; must not be blank. */
    @NotBlank(message = "Name is required")
    private String name;

    /** Email address; must be a valid format and not blank. */
    @Email(message = "A valid email is required")
    @NotBlank
    private String email;

    /** Raw password; must not be blank. Will be BCrypt-hashed before storage. */
    @NotBlank(message = "Password is required")
    private String password;
}
