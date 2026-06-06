package com.tradingapp.financialsimulator.controller;

import com.tradingapp.financialsimulator.dto.RegisterRequest;
import com.tradingapp.financialsimulator.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Controller to handle authentication-related endpoints, such as user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        authenticationService.register(request);

        return ResponseEntity.ok("User registered successfully !");

    }
}
