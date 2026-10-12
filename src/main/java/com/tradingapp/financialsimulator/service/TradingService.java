package com.tradingapp.financialsimulator.service;

import com.influxdb.client.domain.Run;
import com.tradingapp.financialsimulator.dto.AdvancedOrderRequestDTO;
import com.tradingapp.financialsimulator.dto.MarketOrderRequestDTO;
import com.tradingapp.financialsimulator.dto.OrderSummaryDTO;
import com.tradingapp.financialsimulator.model.*;
import com.tradingapp.financialsimulator.model.enums.OrderSide;
import com.tradingapp.financialsimulator.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradingService {

    private static final Logger logger =
            LoggerFactory.getLogger(TradingService.class);

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataCache marketDataCache;


    @Transactional
    public Order placeMarketOrder(
            MarketOrderRequestDTO orderRequest,
            String username) {

        // =========================================================
        // 1. FIND USER
        // =========================================================

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + username
                        )
                );


        // =========================================================
        // 2. FIND STOCK
        // =========================================================

        Stock stock = stockRepository
                .findBySymbol(orderRequest.getSymbol())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Stock not found: "
                                        + orderRequest.getSymbol()
                        )
                );


        // =========================================================
        // 3. GET CURRENT MARKET PRICE
        // =========================================================

        BigDecimal marketPrice =
                marketDataCache
                        .getPrice(stock.getSymbol())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Market price not available for "
                                                + stock.getSymbol()
                                )
                        );


        // =========================================================
        // 4. FIND USER PORTFOLIO
        // =========================================================

        Portfolio portfolio =
                portfolioRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Portfolio not found for user ID: "
                                                + user.getId()
                                )
                        );


        // =========================================================
        // 5. VALIDATE QUANTITY
        // =========================================================

        if (orderRequest.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero."
            );
        }


        // =========================================================
        // 6. CALCULATE TOTAL COST
        // =========================================================


      


        BigDecimal totalCost =
                marketPrice.multiply(BigDecimal.valueOf(orderRequest.getQuantity()));


        // =========================================================
        // 7. PROCESS BUY / SELL
        // =========================================================

        if (orderRequest.getOrderSide() == OrderSide.BUY) {

            processBuyOrder( 
                    portfolio,
                    stock,
                    BigDecimal.valueOf(orderRequest.getQuantity()),
                    marketPrice,
                    totalCost
            );

        } else if (orderRequest.getOrderSide() == OrderSide.SELL) {

            processSellOrder(
                    portfolio,
                    stock,
                    orderRequest.getQuantity(),
                    marketPrice,
                    totalCost
            );

        } else {

            throw new RuntimeException(
                    "Invalid order type."
            );
        }


        // =========================================================
        // 8. CREATE TRANSACTION
        // =========================================================

        Transaction transaction =
                createTransaction(
                        portfolio,
                        stock,
                        orderRequest,
                        marketPrice
                );

        transactionRepository.save(transaction);


        // =========================================================
        // 9. CREATE ORDER
        // =========================================================

        Order order =
                createOrder(
                        portfolio,
                        stock,
                        orderRequest,
                        marketPrice,
                        transaction
                );


        // =========================================================
        // 10. SAVE ORDER
        // =========================================================

        return orderRepository.save(order);
    }


    // =============================================================
    // BUY ORDER
    // =============================================================

    private void processBuyOrder(
            Portfolio portfolio,
            Stock stock,
            BigDecimal quantity,
            BigDecimal marketPrice,
            BigDecimal totalCost) {

        logger.info(
                "Processing BUY order for user {}, stock {}, quantity {}",
                portfolio.getUser().getUsername(),
                stock.getSymbol(),
                quantity
        );


        // Check balance
        if (portfolio.getCashBalance()
                .compareTo(totalCost) < 0) {

            throw new RuntimeException(
                    "Insufficient funds to place buy order."
            );
        }


        // Deduct money
        portfolio.setCashBalance(portfolio.getCashBalance() .subtract(totalCost)
        );

        portfolioRepository.save(portfolio);


        BigDecimal orderQuantity =
                (quantity);


        // Find existing asset or create new asset
        PortfolioAsset asset =
                portfolioAssetRepository
                        .findByPortfolioAndStock(
                                portfolio,
                                stock
                        )
                        .orElseGet(() ->
                                PortfolioAsset.builder()
                                        .portfolio(portfolio)
                                        .stock(stock)
                                        .quantity(BigDecimal.ZERO)
                                        .averageBuyPrice(BigDecimal.ZERO)
                                        .build()
                        );


        // Existing investment
        BigDecimal existingInvestment =
                asset.getAverageBuyPrice()
                        .multiply(asset.getQuantity());


        // New total investment
        BigDecimal newTotalInvestment =
                existingInvestment.add(totalCost);


        // New quantity
        BigDecimal newQuantity =
                asset.getQuantity()
                        .add(orderQuantity);


        // Update asset
        asset.setQuantity(newQuantity);

        asset.setAverageBuyPrice(
                newTotalInvestment.divide(
                        newQuantity,
                        2,
                        RoundingMode.HALF_UP
                )
        );


        portfolioAssetRepository.save(asset);
    }


    // =============================================================
    // SELL ORDER
    // =============================================================

    private void processSellOrder(
            Portfolio portfolio,
            Stock stock,
            int quantity,
            BigDecimal marketPrice,
            BigDecimal proceeds) {

        logger.info(
                "Processing SELL order for user {}, stock {}, quantity {}",
                portfolio.getUser().getUsername(),
                stock.getSymbol(),
                quantity
        );


        // Find asset
        PortfolioAsset asset =
                portfolioAssetRepository
                        .findByPortfolioAndStock(
                                portfolio,
                                stock
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User does not own this stock."
                                )
                        );


        BigDecimal orderQuantity =
                BigDecimal.valueOf(quantity);


        // Check shares
        if (asset.getQuantity()
                .compareTo(orderQuantity) < 0) {

            throw new RuntimeException(
                    "Insufficient shares to place sell order."
            );
        }


        // Add money to portfolio
        portfolio.setCashBalance(
                portfolio.getCashBalance()
                        .add(proceeds)
        );

        portfolioRepository.save(portfolio);


        // Remaining shares
        BigDecimal newQuantity =
                asset.getQuantity()
                        .subtract(orderQuantity);


        // Remove asset if zero
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {

            portfolioAssetRepository.delete(asset);

        } else {

            asset.setQuantity(newQuantity);

            portfolioAssetRepository.save(asset);
        }
    }


    // =============================================================
    // CREATE TRANSACTION
    // =============================================================

    private Transaction createTransaction(
            Portfolio portfolio,
            Stock stock,
            MarketOrderRequestDTO orderRequest,
            BigDecimal price) {

        return Transaction.builder()

                // IMPORTANT
                .user(portfolio.getUser())
                

                .stock(stock)

                .type(
                        orderRequest.getOrderSide() == OrderSide.BUY
                                ? TransactionType.BUY
                                : TransactionType.SELL
                )

                .quantity(
                        BigDecimal.valueOf(
                                orderRequest.getQuantity()
                        )
                )

                // IMPORTANT
                // This fixes:
                // not-null property references a null value:
                // Transaction.price
                .price(price)

                .timestamp(LocalDateTime.now())

                .build();
    }


    // =============================================================
    // CREATE ORDER
    // =============================================================

    private Order createOrder(
            Portfolio portfolio,
            Stock stock,
            MarketOrderRequestDTO orderRequest,
            BigDecimal price,
            Transaction transaction) {

        return Order.builder()

                .user(portfolio.getUser())

                .portfolio(portfolio)

                .stock(stock)
                .side(orderRequest.getOrderSide())

                .type(OrderType.MARKET)

                .status(OrderStatus.EXECUTED)

                .quantity(
                        BigDecimal.valueOf(
                                orderRequest.getQuantity()
                        )
                )

                .price(price)

                .transaction(transaction)

                .build();
    }

    // Method for the Advanced Details
    @Transactional
    public Order placedAdvancedOrder(AdvancedOrderRequestDTO orderRequestDTO,String username){
       // 1. Validate the entity exist
        User user=userRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("User not found "+username));

        Stock stock=stockRepository.findBySymbol(orderRequestDTO.getSymbol())
                .orElseThrow(()->new RuntimeException("Stock symbol not found "+orderRequestDTO.getOrderType()));

        // 2.perform basic validation on itself
        if (orderRequestDTO.getTriggerPrice().compareTo(BigDecimal.ZERO)<=0){
            throw new RuntimeException("Trigger prices must be positive");
        }
        if (orderRequestDTO.getOrderType()==OrderType.MARKET){
            throw new RuntimeException("This end point for Advanced order (LIMIT,STOP_LOSS) only");
        }

        Order pendingOrder=Order.builder()
                .portfolio(user.getPortfolio()).stock(stock)
                .side(orderRequestDTO.getSide())
                .type(orderRequestDTO.getOrderType())
                .status(OrderStatus.PENDING)
                .quantity(orderRequestDTO.getQuantity())
                .price(orderRequestDTO.getTriggerPrice())
                .triggerPrice(orderRequestDTO.getTriggerPrice())
                .createdAt(LocalDateTime.now())
                .build();

        logger.info("Placing the PENDING {} {} order for {} shares of {} @ trigger price {}",
                pendingOrder.getSide(),pendingOrder.getType(),pendingOrder.getQuantity(),
                pendingOrder.getStock().getSymbol(),pendingOrder.getTriggerPrice());

        return orderRepository.save(pendingOrder);
    }

    @Transactional
    public void executeTriggeredOrder(Long orderId) {
//Fetch the order and lock it for the transation to prevent race condition
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order not found By Id "+orderId));

                // This prevent the double -execution if the processor runs again before the lock
        if (order.getStatus()!=OrderStatus.PENDING) {
            logger.warn("Attempted to execute order id{} which is not PENDING, Current status ",order.getId(),order.getStatus());
            return;
        }

        logger.info("Executing trigger order id :{}",orderId);
        String symbol=order.getStock().getSymbol();
        Portfolio portfolio=order.getPortfolio();

        // Get the current market price of execution
        BigDecimal executionPrice=marketDataCache.getPrice(symbol)
                .orElseThrow(()->new RuntimeException("Market price is not available for "+symbol));

        BigDecimal totalValue=executionPrice.multiply(order.getQuantity() );

        // Final validation. check funds/share att the moment of execution
        try{
            if (order.getSide()==OrderSide.BUY){
                //This re-uses the same validation ans logic as market orders
                processBuyOrder(portfolio,order.getStock(),order.getQuantity(),executionPrice,totalValue);
            }else {
                processBuyOrder(portfolio,order.getStock(),order.getQuantity(),executionPrice,totalValue);
            }

            Transaction transaction=createTransactionForExceutedOrder(order,executionPrice);

            transactionRepository.save(transaction);
            order.setStatus(OrderStatus.EXECUTED);
            order.setPrice(executionPrice);
            order.setTransaction(transaction);
            orderRepository.save(order);
            logger.info("SUCCESS: Order{} executed at New portfolio cash ",order.getId(),executionPrice,portfolio.getCashBalance());


        }catch (RuntimeException e){
            logger.warn("FAILED to execute order {}, Setting ststus to FAILED ",order.getId(),e.getMessage());
             order.setStatus(OrderStatus.FAILED);
             orderRepository.save(order);
        }

    }

    // help to create a tranction from an executed order
    private  Transaction createTransactionForExceutedOrder(Order order,BigDecimal executionPrice){
         return Transaction.builder()
                 .portfolio(order.getPortfolio())
                 .stock(order.getStock())
                 .type(order.getSide()==OrderSide.BUY ? TransactionType.BUY:TransactionType.SELL)
                 .quantity(order.getQuantity())
                 .price(executionPrice)
                 .timestamp(LocalDateTime.now())
                 .build();

    }

    public List<OrderSummaryDTO> getPendingOrderForUser(String username){
        User user=userRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException(" User not found "+username));

        List<Order> pendingOrders=orderRepository.findByPortfolioAndStatus(user.getPortfolio(),OrderStatus.PENDING);

        // Map list of Order entities to a list of Ordersummmary
        return pendingOrders.stream()
                .map(order -> new OrderSummaryDTO(
                        order.getId(),
                        order.getStock().getSymbol()
                                ,order.getType(),
                        order.getSide(),
                        order.getStatus(),
                        order.getQuantity(),
                        order.getCreateAt(),
                        order.getTriggerPrice()
                )).collect(Collectors.toList());
    }
}