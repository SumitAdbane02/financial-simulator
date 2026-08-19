package com.tradingapp.financialsimulator.service;

import com.tradingapp.financialsimulator.dto.AuthenticationResponse;
import com.tradingapp.financialsimulator.dto.LoginRequest;
import com.tradingapp.financialsimulator.dto.RegisterRequest;
import com.tradingapp.financialsimulator.model.Portfolio;
import com.tradingapp.financialsimulator.model.Role;
import com.tradingapp.financialsimulator.model.User;
import com.tradingapp.financialsimulator.repository.PortfolioRepository;
import com.tradingapp.financialsimulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    // =========================
    // REGISTER
    // =========================
    public void register(RegisterRequest request) {

        // Create User
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // Save User first
        User savedUser = userRepository.save(user);

        // Create Portfolio for the newly registered user
        Portfolio portfolio = Portfolio.builder()
                .user(savedUser)
                .cashBalance(new BigDecimal("100000.00"))
                .build();

        // Set portfolio in user as well
        savedUser.setPortfolio(portfolio);

        // Save Portfolio
        portfolioRepository.save(portfolio);
    }


    // =========================
    // LOGIN
    // =========================
    public AuthenticationResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Generate JWT
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}