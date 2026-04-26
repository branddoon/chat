package com.chat.controllers;

import com.chat.dto.AuthResponse;
import com.chat.dto.LoginRequest;
import com.chat.dto.RegisterRequest;
import com.chat.helpers.JwtHelper;
import com.chat.models.User;
import com.chat.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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

    /** When true the cookie is flagged Secure (HTTPS only). Set false for local HTTP dev. */
    @Value("${app.cookie.secure:true}")
    private boolean secureCookie;

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
     * Writes the JWT as an {@code HttpOnly; SameSite=Strict} cookie on the response.
     * The cookie is never accessible from JavaScript, eliminating the XSS token-theft
     * vector present when tokens are stored in {@code localStorage}.
     *
     * <p>Set {@code app.cookie.secure=false} in {@code application.yml} for local HTTP
     * development; always {@code true} in production (HTTPS).</p>
     *
     * @param response the HTTP response to attach the cookie to
     * @param token    the signed JWT value
     */
    private void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofHours(8))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Clears the {@code access_token} cookie by setting its {@code Max-Age} to zero.
     *
     * @param response the HTTP response to attach the expired cookie to
     */
    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
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
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody RegisterRequest req,
                                                   HttpServletResponse response) {
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
            log.info("Finished user creating process...");
            setAuthCookie(response, token);
            return ResponseEntity.ok(
                    AuthResponse.success(user.getEmail(), user.getName(), user.getId()));
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletResponse response) {
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
            setAuthCookie(response, token);
            return ResponseEntity.ok(
                    AuthResponse.success(user.getEmail(), user.getName(), user.getId()));

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
    public ResponseEntity<AuthResponse> renewToken(Authentication authentication,
                                                   HttpServletResponse response) {
        String uid   = (String) authentication.getPrincipal();
        User user    = userRepository.findById(uid).orElseThrow();
        String token = jwtHelper.generateJWT(uid);
        setAuthCookie(response, token);
        return ResponseEntity.ok(
                new AuthResponse(true, user.getEmail(), user.getName(), user.getId(), "renew"));
    }

    /**
     * Logs the current user out by expiring the {@code access_token} cookie.
     * The client's browser will discard the cookie immediately.
     *
     * <p>This endpoint is intentionally permit-all so that a client with an
     * expired or invalid token can still clear its cookie cleanly.</p>
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return ResponseEntity.ok().build();
    }
}
