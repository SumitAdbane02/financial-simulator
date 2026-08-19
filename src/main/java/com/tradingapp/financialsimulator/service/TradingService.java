// highlight-start
package com.tradingapp.financialsimulator.service;

import com.tradingapp.financialsimulator.dto.MarketOrderRequestDTO;
import com.tradingapp.financialsimulator.model.*;
import com.tradingapp.financialsimulator.model.OrderStatus;
import com.tradingapp.financialsimulator.model.enums.OrderType;
//import com.tradingapp.financialsimulator.model.OrderType;
import com.tradingapp.financialsimulator.model.TransactionType;
import com.tradingapp.financialsimulator.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradingService {

    private static final Logger logger = LoggerFactory.getLogger(TradingService.class);

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataCache marketDataCache;

    /**
     * Places a market order (BUY or SELL).
     * This entire method is marked as @Transactional. This means all the database operations
     * within it (updating portfolio, adding assets, creating transactions/orders) are part of a
     * single atomic transaction. If any part fails (e.g., due to a validation error throwing an
     * exception), the entire transaction will be rolled back, ensuring data integrity.
     *
     * @param orderRequest The DTO containing order details (symbol, quantity, type).
     * @param username     The username of the user placing the order.
     * @return The created and executed Order entity.
     */
    @Transactional
    public Order placeMarketOrder(MarketOrderRequestDTO orderRequest, String username) {
        // 1. VALIDATION: Ensure all entities and data points exist before proceeding.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username)); // Replace with custom exception

        Stock stock = stockRepository.findBySymbol(orderRequest.getSymbol())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + orderRequest.getSymbol())); // Replace with custom exception

        BigDecimal marketPrice = marketDataCache.getPrice(stock.getSymbol())
                .orElseThrow(() -> new RuntimeException("Market price not available for " + stock.getSymbol())); // Replace with custom exception

        Portfolio portfolio = portfolioRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Portfolio not found for user ID: " + user.getId()
                ));

        portfolio.getUser();
        BigDecimal totalCost = marketPrice.multiply(BigDecimal.valueOf(orderRequest.getQuantity()));

        // 2. PROCESS ORDER based on type (BUY/SELL)
        if (orderRequest.getOrderType() == OrderType.BUY) {
            processBuyOrder(portfolio, stock, orderRequest.getQuantity(), marketPrice, totalCost);
        } else {
            processSellOrder(portfolio, stock, orderRequest.getQuantity(), marketPrice, totalCost);
        }

        // 3. RECORD a permanent transaction log.
        Transaction transaction = createTransaction( stock, orderRequest);
        transactionRepository.save(transaction);

        // 4. CREATE the final order record with an EXECUTED status.
        Order order = createOrder(portfolio, stock, orderRequest, marketPrice, transaction);
        return orderRepository.save(order);
    }



    private void processBuyOrder(Portfolio portfolio, Stock stock, int quantity,
                                 BigDecimal marketPrice, BigDecimal totalCost) {

        logger.info("Processing BUY order for user {}, stock {}, quantity {}",
                portfolio.getUser().getUsername(), stock.getSymbol(), quantity);

        // Validation: Does the user have enough cash?
        if (portfolio.getCashBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient funds to place buy order.");
        }

        // Deduct cash from portfolio
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalCost));
        portfolioRepository.save(portfolio);

        // Convert order quantity to BigDecimal
        BigDecimal orderQuantity = BigDecimal.valueOf(quantity);

        // Find existing asset or create a new one
        PortfolioAsset asset = portfolioAssetRepository
                .findByPortfolioAndStock(portfolio, stock)
                .orElseGet(() -> PortfolioAsset.builder()
                        .portfolio(portfolio)
                        .stock(stock)
                        .quantity(BigDecimal.ZERO)
                        .averageBuyPrice(BigDecimal.ZERO)
                        .build());

        // Calculate total invested amount before this purchase
        BigDecimal existingInvestment = asset.getAverageBuyPrice()
                .multiply(asset.getQuantity());

        // Calculate new total investment
        BigDecimal newTotalInvestment = existingInvestment.add(totalCost);

        // Calculate new quantity
        BigDecimal newQuantity = asset.getQuantity().add(orderQuantity);

        // Update asset
        asset.setQuantity(newQuantity);
        asset.setAverageBuyPrice(
                newTotalInvestment.divide(newQuantity, 2, RoundingMode.HALF_UP)
        );

        portfolioAssetRepository.save(asset);
    }

    private void processSellOrder(Portfolio portfolio, Stock stock, int quantity,
                                  BigDecimal marketPrice, BigDecimal proceeds) {

        logger.info("Processing SELL order for user {}, stock {}, quantity {}",
                portfolio.getUser().getUsername(), stock.getSymbol(), quantity);

        // Validation: Does the user own this stock?
        PortfolioAsset asset = portfolioAssetRepository
                .findByPortfolioAndStock(portfolio, stock)
                .orElseThrow(() ->
                        new RuntimeException("User does not own this stock."));

        // Convert quantity to BigDecimal
        BigDecimal orderQuantity = BigDecimal.valueOf(quantity);

        // Validation: Does the user own enough shares?
        if (asset.getQuantity().compareTo(orderQuantity) < 0) {
            throw new RuntimeException("Insufficient shares to place sell order.");
        }

        // Add sale proceeds to portfolio cash
        portfolio.setCashBalance(portfolio.getCashBalance().add(proceeds));
        portfolioRepository.save(portfolio);

        // Calculate remaining quantity
        BigDecimal newQuantity = asset.getQuantity().subtract(orderQuantity);

        // Remove asset if all shares have been sold
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            portfolioAssetRepository.delete(asset);
        } else {
            asset.setQuantity(newQuantity);
            portfolioAssetRepository.save(asset);
        }
    }

    private Transaction createTransaction( Stock stock, MarketOrderRequestDTO orderRequest) {
        return Transaction.builder()
//                .portfolio(portfolio)
                .stock(stock)
                .type(orderRequest.getOrderType() == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL)
                .quantity(BigDecimal.valueOf(orderRequest.getQuantity()))
//                .pricePerShare(price)
                .timestamp(LocalDateTime.now())
                .build();
    }
    private Order createOrder(Portfolio portfolio,
                              Stock stock,
                              MarketOrderRequestDTO orderRequest,
                              BigDecimal price,
                              Transaction transaction) {

        return Order.builder()
                .user(portfolio.getUser())
                .portfolio(portfolio)
                .stock(stock)
                .type(orderRequest.getOrderType())
                .status(OrderStatus.EXECUTED)
                .quantity(BigDecimal.valueOf(orderRequest.getQuantity()))
                .price(price)
                .transaction(transaction)
                .build();
    }
}
// highlight-end