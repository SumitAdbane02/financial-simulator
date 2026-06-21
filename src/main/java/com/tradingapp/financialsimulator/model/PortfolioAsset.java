package com.tradingapp.financialsimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an asset held within a user's portfolio.
 * This class acts as a join table between Portfolio and Stock,
 * with the additional attribute of 'quantity' to represent the number of shares owned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfolio_assets")
public class PortfolioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A portfolio asset must belong to a portfolio. This is the 'many' side of the One-to-Many relationship.
    // The portfolio is the owner of this asset.
    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetching is a performance best practice.
    @JoinColumn(name = "portfolio_id", nullable = false) // This creates the foreign key column 'portfolio_id'.
    private Portfolio portfolio;

    // A portfolio asset must be a specific stock.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false) // This creates the foreign key column 'stock_id'.
    private Stock stock;

    // The quantity of the stock owned. Using BigDecimal for precision,
    // which is essential for financial data and allows for fractional shares.
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

}