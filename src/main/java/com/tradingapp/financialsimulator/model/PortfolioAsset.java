package com.tradingapp.financialsimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a specific stock holding within a user's portfolio.
 * This entity acts as a join table between Portfolio and Stock
 * and stores additional information about the holding, such as
 * quantity owned and average purchase price.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfolio_assets")
public class PortfolioAsset {

    // Unique identifier for each portfolio asset record.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The portfolio to which this asset belongs.
     * Many PortfolioAsset records can belong to a single Portfolio.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /**
     * The stock associated with this portfolio asset.
     * Many PortfolioAsset records can reference the same Stock.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * The number of shares currently owned by the user.
     * BigDecimal is used to support fractional shares and
     * maintain precision for financial calculations.
     */
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    /**
     * The average price at which the shares were purchased.
     * This value is recalculated whenever additional shares
     * of the same stock are bought.
     *
     * Example:
     * Buy 10 shares at $100 and 5 shares at $120.
     * Average Buy Price = ((10 * 100) + (5 * 120)) / 15
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal averageBuyPrice;
}