package com.tradingapp.financialsimulator.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for all JWT-related operations:
 * - Generating tokens
 * - Validating tokens
 * - Extracting claims from tokens
 */
@Service
public class JwtService {

    // Injecting the secret key from application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Extracts the username from a JWT token.
     * @param token The JWT token.
     * @return The username (subject) from the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * A generic function to extract a specific claim from a token.
     * @param token The JWT token.
     * @param claimsResolver A function that specifies how to extract the desired claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT for a given user.
     * @param userDetails The user details for whom the token is to be generated.
     * @return A signed JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT with extra claims.
     * @param extraClaims Additional claims to be included in the token payload.
     * @param userDetails The user details.
     * @return A signed JWT string.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // The subject is typically the user's unique identifier.
                .setIssuedAt(new Date(System.currentTimeMillis())) // The time the token was created.
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Expiration time (e.g., 10 hours).
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign the token with the secret key and HS256 algorithm.
                .compact();
    }

    /**
     * Validates a JWT token.
     * @param token The token to validate.
     * @param userDetails The user details to validate against.
     * @return True if the token is valid for the given user, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // A token is valid if the username matches and the token has not expired.
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the JWT token to extract all its claims (the payload).
     * @param token The JWT token.
     * @return The Claims object containing the payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Decodes the Base64 secret key and returns it as a Key object.
     * @return The signing key.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}