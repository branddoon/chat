package com.chat.middleware;

import com.chat.helpers.JwtHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Servlet filter that reads and validates the JWT from the {@code x-token} request header.
 * When the token is valid, the authenticated user's ID is stored in the Spring Security
 * context so that downstream handlers can access it via
 * {@link org.springframework.security.core.Authentication#getPrincipal()}.
 *
 * <p>Invalid or missing tokens are silently ignored; Spring Security will then reject
 * the request if the matched route requires authentication.</p>
 *
 * Executes once per request (extends {@link OncePerRequestFilter}).
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;

    /**
     * @param jwtHelper JWT generation and validation utility
     */
    public JwtFilter(JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    /**
     * Resolves the JWT from the request using the following priority:
     * <ol>
     *   <li>{@code access_token} HttpOnly cookie (browser HTTP requests)</li>
     *   <li>{@code x-token} header (Socket.IO handshake and direct API calls)</li>
     * </ol>
     * When a valid token is found, the authenticated user's ID is stored in the
     * Spring Security context so that downstream handlers can access it via
     * {@link org.springframework.security.core.Authentication#getPrincipal()}.
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the remaining filter chain to execute
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && !token.isBlank()) {
            try {
                String uid = jwtHelper.getUidFromToken(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // Invalid token — Spring Security will deny the request if the route is protected.
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Reads the JWT from the {@code access_token} HttpOnly cookie first.
     * Falls back to the {@code x-token} request header if the cookie is absent.
     *
     * @param request the incoming HTTP request
     * @return the raw JWT string, or {@code null} if neither source contains a token
     */
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> "access_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .orElse(request.getHeader("x-token"));
        }
        return request.getHeader("x-token");
    }
}
