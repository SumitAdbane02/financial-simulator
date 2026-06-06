package com.tradingapp.financialsimulator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
/**
 * A REST controller that provides a simple health check endpoint.
 * This is a standard practice to verify if the application is running and responsive.
 */

@RestController
// @RequestMapping sets the base path for all endpoints defined in this controller.
// All endpoints in this class will be prefixed with "/api/health".
@RequestMapping("/api/health")
public class HealthCheckController {
    /**
     * Handles HTTP GET requests to the /api/health endpoint.
     *
     * @return A ResponseEntity containing a JSON object with the application's status.
     */
    @GetMapping
    public ResponseEntity<Map<String,String>> healthCheck(){
      // We create a Map to structure our response. This will be automatically converted to a JSON object.
        // For example: {"status": "Application is up and running!"}
        // Using a structured response like JSON is much more flexible for API clients than a simple string.

        Map<String,String> response= Collections.singletonMap("status","Application is up and running !");

        // ResponseEntity.ok() creates a response with an HTTP status code of 200 (OK).
        // The body of the response will be the 'response' map.

        return ResponseEntity.ok(response);
    }
}
