package com.tradingapp.financialsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Data Transfer Object (DTO) that represents the payload for a user registration request.
 * Using a DTO helps to decouple the API layer from the persistence layer (entities).
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    // The desired username for the new account.
    private String username;

    // The email address for the new account.
    private String email;

    // The plain-text password for the new account. This will be hashed by the server.
    private String password;
}
