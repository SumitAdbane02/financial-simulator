package com.tradingapp.financialsimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a tradable stock in the simulation.
 * This entity serves as a master list of all available stocks,
 * storing their static information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stocks") // We name the table "stocks" following a plural naming convention.
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The ticker symbol is the unique identifier for a stock in the market (e.g., "AAPL", "GOOGL").
    // We enforce uniqueness at the database level to ensure data integrity.
    // It is non-nullable and has a defined length for consistency.
    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    // The full name of the company or asset (e.g., "Apple Inc.").
    @Column(nullable = false)
    private String name;

    // The stock exchange where the stock is listed (e.g., "NASDAQ", "NYSE").
    // This is useful metadata for display and potential future filtering.
    @Column(nullable = false, length = 50)
    private String exchange;

}