package com.tradingapp.financialsimulator.dto;

import com.tradingapp.financialsimulator.model.enums.OrderSide;
import lombok.Data;

/**
 * DTO for receiving a market order request from the client.
 */
@Data
public class MarketOrderRequestDTO {
    private String symbol;
    private int quantity;
    private OrderSide orderSide; // Will be either BUY or SELL
}