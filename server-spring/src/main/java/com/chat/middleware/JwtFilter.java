package com.chat.middleware;

import com.chat.helpers.JwtHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
     * Extracts the {@code x-token} header, validates the JWT, and populates
     * the Spring Security context with the authenticated user's ID.
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

        String token = request.getHeader("x-token");

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
}
