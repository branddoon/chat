package com.chat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/login}.
 *
 */
@Data
public class LoginRequest {

    /** Email address; must be a valid format and not blank. */
    @Email(message = "A valid email is required")
    @NotBlank
    private String email;

    /** Raw password to verify against the stored BCrypt hash. */
    @NotBlank(message = "Password is required")
    private String password;
}
