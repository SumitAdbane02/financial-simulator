package com.tradingapp.financialsimulator.service;

import com.tradingapp.financialsimulator.dto.AuthenticationResponse;
import com.tradingapp.financialsimulator.dto.LoginRequest;
import com.tradingapp.financialsimulator.dto.RegisterRequest;
import com.tradingapp.financialsimulator.model.Portfolio;
import com.tradingapp.financialsimulator.model.Role;
import com.tradingapp.financialsimulator.model.User;
import com.tradingapp.financialsimulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service class to handle user authentication-related business logic,
 * such as registration and login.
 */
@Service
@RequiredArgsConstructor // Lombok's way of creating a constructor for all final fields, enabling constructor injection.
public class AuthenticationService {

    // Injecting the UserRepository to interact with the user's data in the database.
    private final UserRepository userRepository;
    // Injecting the PasswordEncoder bean we defined in SecurityConfig to hash passwords.
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;
    /**
     * Registers a new user in the system.
     * @param request The registration request containing user details.
     * @return The newly created User object.
     */
    public User register(RegisterRequest request){
        // First, check if a user with the given username or email already exists.
        // This prevents duplicates and is a crucial validation step.

        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email is already in use");

        }
// Create a new User object using the builder pattern.
        User user=User.builder().username(request.getUsername())
                .email(request.getEmail())
                // IMPORTANT: Hash the plain-text password before saving it to the database.
                .password(passwordEncoder.encode(request.getPassword()))
        // Assign a default role to the new user.
               .role(Role.USER).build();

// highlight-start
        // Create a new portfolio for the user with a starting cash balance.
        // Let's give every new user $100,000 of virtual currency to start trading.
        Portfolio portfolio = Portfolio.builder()
                .user(user) // Link the portfolio back to the user.
                .cashBalance(new BigDecimal("100000.00"))
                .build();

        // Set the portfolio on the user object to establish the bidirectional link.
        user.setPortfolio(portfolio);

        return userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user =userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new IllegalStateException("User not found after authentication. This should not happen,"));

        var jwtToken =jwtService.generateToken(user);
        //
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
