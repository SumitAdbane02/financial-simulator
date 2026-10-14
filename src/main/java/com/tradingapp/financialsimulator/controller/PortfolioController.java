package com.tradingapp.financialsimulator.controller;

import com.tradingapp.financialsimulator.dto.PortfolioDetailsDTO;
import com.tradingapp.financialsimulator.dto.PortfolioPerformanceDTO;
import com.tradingapp.financialsimulator.dto.PortfolioUpdateDTO;
import com.tradingapp.financialsimulator.model.PortfolioAsset;
import com.tradingapp.financialsimulator.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<PortfolioDetailsDTO> getPortfolioDetails(Authentication authentication){
        try {
            //ever access their own portfolio data
            String username = authentication.getName();

            // Delegate the business logic to the portfolioService
            PortfolioDetailsDTO portfolioDetails=portfolioService.getPortfolioDetails( username);
            // Return the dto
            return ResponseEntity.ok(portfolioDetails);
        }catch (RuntimeException e){

            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/performance")
    public ResponseEntity<PortfolioPerformanceDTO> getPortfolioPerformance(Authentication authentication,
                                                                @RequestParam(defaultValue = "30d")String range){
        try {
            String username=authentication.getName();
            // A simple way to parse the range
            int days=Integer.parseInt(range.toLowerCase().replace("d",""));

            PortfolioPerformanceDTO performanceData=portfolioService.getPortfolioPerformance(username,days);
            return ResponseEntity.ok(performanceData);
        }catch (NumberFormatException e){
            return ResponseEntity.badRequest().build();

        }catch (RuntimeException e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
