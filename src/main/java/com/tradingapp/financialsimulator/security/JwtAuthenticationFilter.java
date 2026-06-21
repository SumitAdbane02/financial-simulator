package com.tradingapp.financialsimulator.security;

import com.tradingapp.financialsimulator.service.JwtService;
import com.tradingapp.financialsimulator.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A custom Spring Security filter that intercepts all incoming HTTP requests
 * to check for a valid JWT in the 'Authorization' header.
 *
 * If a valid token is found, it authenticates the user and sets the
 * security context for the duration of the request.
 */
@Component // Marks this class as a Spring component, so it can be managed by the Spring container.
@RequiredArgsConstructor // Lombok: Creates a constructor for all final fields.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * The main filtering logic. This method is executed for each incoming request.
     *
     * @param request The incoming HTTP request.
     * @param response The HTTP response.
     * @param filterChain The chain of filters to pass the request along.
     * @throws ServletException If a servlet-related error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Get the Authorization header from the request.
        final String authHeader = request.getHeader("Authorization");

        // 2. Check if the header is present and follows the "Bearer " scheme.
        // If not, we pass the request to the next filter in the chain and stop processing.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the JWT from the header.
        final String jwt = authHeader.substring(7); // "Bearer " is 7 characters long.

        // 4. Extract the username (subject) from the token using our JwtService.
        final String username = jwtService.extractUsername(jwt);

        // 5. Check if we have a username and if the user is not already authenticated.
        // The second check (SecurityContextHolder.getContext().getAuthentication() == null) is important
        // to ensure we only perform the authentication logic once per request.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 6. Load the user details from the database using our UserDetailsServiceImpl.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. Validate the token against the loaded user details.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 8. If the token is valid, create an authentication token.
                // This is the object Spring Security uses to represent the authenticated user.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are not needed as the user is already authenticated by the token.
                        userDetails.getAuthorities()
                );

                // 9. Enhance the authentication token with details from the HTTP request.
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. Update the SecurityContextHolder with the new authentication token.
                // This is the crucial step that marks the current user as authenticated.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Pass the request and response to the next filter in the chain.
        filterChain.doFilter(request, response);
    }
}