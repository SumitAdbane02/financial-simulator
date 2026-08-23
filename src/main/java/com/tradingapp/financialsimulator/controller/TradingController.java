package com.tradingapp.financialsimulator.controller;

import com.tradingapp.financialsimulator.dto.MarketOrderRequestDTO;
import com.tradingapp.financialsimulator.model.Order;
import com.tradingapp.financialsimulator.service.MarketDataCache;
import com.tradingapp.financialsimulator.service.TradingService;
import lombok.RequiredArgsConstructor;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TradingController {

    private final TradingService tradingService;

    @PostMapping("/market")
    public ResponseEntity<?> placeMarketOrder(
            @RequestBody MarketOrderRequestDTO orderRequest,
            Authentication authentication) {

        // The 'Authentication' object is our gateway to the user's identity.
        // It's securely provided by Spring Security's context after validating the JWT.
        String username = authentication.getName();

        try {
            // Delegate all the complex business logic to the TradingService.
            Order executedOrder = tradingService.placeMarketOrder(orderRequest, username);

            // Return a successful response. HTTP 201 Created is the most appropriate status
            // for a POST request that results in the creation of a new resource.
            return ResponseEntity.status(HttpStatus.CREATED).body(executedOrder);

        } catch (RuntimeException e) {
            // In a production app, a @ControllerAdvice would handle this more globally.
            // For now, we catch the exceptions thrown by the service (e.g., "Insufficient funds")
            // and return a 400 Bad Request status with the error message.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
