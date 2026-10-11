package com.tradingapp.financialsimulator.scheduler;

import com.tradingapp.financialsimulator.model.Order;
import com.tradingapp.financialsimulator.model.OrderStatus;
import com.tradingapp.financialsimulator.model.OrderType;
import com.tradingapp.financialsimulator.model.enums.OrderSide;
import com.tradingapp.financialsimulator.repository.OrderRepository;
import com.tradingapp.financialsimulator.service.MarketDataCache;
import com.tradingapp.financialsimulator.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PendingOrderProcessor {

    private static final Logger logger= LoggerFactory.getLogger(PendingOrderProcessor.class);

    //Fetch the all pending order from database
    private final OrderRepository orderRepository;

    // To get latest price of a stock to compare against the trigger price
    private final MarketDataCache marketDataCache;

    // To execute the trade if an order's condition are met
    private final TradingService tradingService;


    @Scheduled(fixedDelay = 5000)
    public void processPendingOrders(){
        logger.info("--> Running Pending Order Processor...");

        // By using repo method we fetch the all PENDING Orders.
        List<Order> pendingOrder=orderRepository.findByStatus(OrderStatus.PENDING);

        if (pendingOrder.isEmpty()){
        logger.trace("No pending orders to process.");
        return;
        }
        logger.trace("Found {}  pending Order to process.",pendingOrder.size());

        // Iterate through each pending order to evaluate it individually
        for (Order order : pendingOrder){
            try{
                String symbol=order.getStock().getSymbol();
                // fetch the latest market price form cache
                Optional<BigDecimal> optionalPrice=marketDataCache.getPrice(symbol);

                // If market price isn't available for any reason, we can't process this order
                if (optionalPrice.isEmpty()){
                    logger.warn("Market price for {} not available in cache .Skipping order id {}.",symbol,order.getId());
                    continue;
                }
                BigDecimal currentMarketPrices=optionalPrice.get();

                boolean shouldTrigger=false;


                if (order.getType()== OrderType.LIMIT){
                    if (order.getSide()== OrderSide.BUY && currentMarketPrices.compareTo(order.getTriggerPrice())<=0){
                        shouldTrigger=true;
                    }else if(order.getSide()==OrderSide.SELL && currentMarketPrices.compareTo(order.getTriggerPrice())>=0){
                        shouldTrigger=true;
                    }
                }
                // STOP_LOSS is a instruction to trade to prevent further losses.
                else if (order.getType()==OrderType.STOP_LOSS) {
                    // A sell stop loss (on a stock you own) triggers if the price drops To or Below the stop price
                   if (order.getSide()==OrderSide.SELL && currentMarketPrices.compareTo(order.getTriggerPrice())<=0){
                       shouldTrigger=true;
                       //
                   } else if (order.getSide()==OrderSide.BUY && currentMarketPrices.compareTo(order.getTriggerPrice())>=0) {
                       shouldTrigger=true;
                       
                   }
                }

                if (shouldTrigger){
                    logger.info(" Triggered: order for share of at trigger price, Current market price ",
                    order.getId(),
                    order.getSide(),
                    order.getQuantity(),
                    order.getStock().getSymbol(),
                    order.getTriggerPrice(),
                    currentMarketPrices);

                    tradingService.executeTriggeredOrder(order.getId());
                }
            }catch (Exception e){
                // Log the exception with the order id for easy debbugging
                logger.error("proccessing pending Order Id {} ",order.getId(),e.getMessage(),e);
            }
        }
    }
}
