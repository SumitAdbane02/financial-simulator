package com.tradingapp.financialsimulator.dto;

import com.tradingapp.financialsimulator.model.enums.OrderType;
import lombok.Data;

/**
 * DTO for receiving a market order request from the client.
 */
@Data
public class MarketOrderRequestDTO {
    private String symbol;
    private int quantity;
    private OrderType orderType; // Will be either BUY or SELL
}