package com.chat.controllers;

import com.chat.dto.AuthResponse;
import com.chat.dto.LoginRequest;
import com.chat.dto.RegisterRequest;
import com.chat.helpers.JwtHelper;
import com.chat.models.User;
import com.chat.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * <ul>
 *   <li>{@code POST /api/login/new} – register a new user account</li>
 *   <li>{@code POST /api/login} – authenticate an existing user</li>
 *   <li>{@code GET /api/login/renew} – renew a valid JWT</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/login")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtHelper jwtHelper;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param userRepository  data access for {@link User} documents
     * @param jwtHelper       JWT generation and validation utility
     * @param passwordEncoder BCrypt encoder for password hashing and verification
     */
    public AuthController(UserRepository userRepository,
                          JwtHelper jwtHelper,
                          PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.jwtHelper       = jwtHelper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user account.
     * Hashes the password with BCrypt, persists the user, and returns a signed JWT.
     *
     * @param req validated registration fields ({@code name}, {@code email}, {@code password})
     * @return {@code 200} with {@link AuthResponse} on success,
     *         {@code 400} if the email is already registered
     */
    @PostMapping("/new")
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody RegisterRequest req) {
        try {
            log.info("Starting creating of new user...");
            if (userRepository.existsByEmail(req.getEmail())) {
                log.info("Email already exists.");
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("The email already exists. Please use a different email."));
            }
            User user = new User();
            user.setName(req.getName());
            user.setEmail(req.getEmail());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userRepository.save(user);
            log.info("User was saved...");
            String token = jwtHelper.generateJWT(user.getId());
            log.info("Generated json web token: {}", token);
            log.info("Finished user creating process...");
            return ResponseEntity.ok(
                    AuthResponse.success(user.getEmail(), user.getName(), user.getId(), token));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(AuthResponse.error("Please contact the administrator."));
        }
    }

    /**
     * Authenticates an existing user with email and password.
     *
     * @param req validated login fields ({@code email}, {@code password})
     * @return {@code 200} with {@link AuthResponse} on success,
     *         {@code 404} if the email is not found,
     *         {@code 400} if the password does not match
     */
    @PostMapping
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            User user = userRepository.findByEmail(req.getEmail()).orElse(null);

            if (user == null) {
                return ResponseEntity.status(404)
                        .body(AuthResponse.error("Email or password not found."));
            }

            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("Email or password not found."));
            }

            String token = jwtHelper.generateJWT(user.getId());

            return ResponseEntity.ok(
                    AuthResponse.success(user.getEmail(), user.getName(), user.getId(), token));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(AuthResponse.error("Please contact the administrator."));
        }
    }

    /**
     * Renews the JWT for the currently authenticated user.
     * Requires a valid {@code x-token} header processed by
     * {@link com.chat.middleware.JwtFilter}.
     *
     * @param authentication Spring Security context populated by {@link com.chat.middleware.JwtFilter}
     * @return {@code 200} with a fresh {@link AuthResponse} containing the new token
     */
    @GetMapping("/renew")
    public ResponseEntity<AuthResponse> renewToken(Authentication authentication) {
        String uid   = (String) authentication.getPrincipal();
        User user    = userRepository.findById(uid).orElseThrow();
        String token = jwtHelper.generateJWT(uid);

        return ResponseEntity.ok(
                new AuthResponse(true, user.getEmail(), user.getName(), user.getId(), token, "renew"));
    }
}
