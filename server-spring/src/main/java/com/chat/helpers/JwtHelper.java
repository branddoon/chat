package com.chat.helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for generating and validating JSON Web Tokens (JWT).
 *
 */
@Component
public class JwtHelper {

    @Value("${jwt.key}")
    private String jwtKey;

    /**
     * Builds the HMAC-SHA signing key from the configured secret.
     *
     * @return the {@link SecretKey} used to sign and verify tokens
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT containing the user's ID as the {@code uid} claim.
     *
     * @param uid the user's MongoDB document ID
     * @return the compact, signed JWT string
     */
    public String generateJWT(String uid) {
        return Jwts.builder()
                .claim("uid", uid)
                .issuedAt(new Date())
                .signWith(getKey())
                .compact();
    }

    /**
     * Validates a JWT and returns the result as a two-element array.
     *
     * @param token the JWT string to validate
     * @return {@code ["true", uid]} on success, {@code ["false", null]} on failure
     */
    public String[] verifyJWT(String token) {
        try {
            String uid = getUidFromToken(token);
            return new String[]{"true", uid};
        } catch (Exception e) {
            return new String[]{"false", null};
        }
    }

    /**
     * Extracts the {@code uid} claim from a valid, signed JWT.
     *
     * @param token the signed JWT string
     * @return the user ID stored in the token's payload
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or tampered with
     */
    public String getUidFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("uid", String.class);
    }
}
