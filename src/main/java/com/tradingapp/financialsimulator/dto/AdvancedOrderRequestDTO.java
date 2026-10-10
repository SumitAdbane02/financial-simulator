package com.tradingapp.financialsimulator.dto;

import com.tradingapp.financialsimulator.model.OrderType;
import com.tradingapp.financialsimulator.model.enums.OrderSide;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdvancedOrderRequestDTO {
    private String symbol;

    private BigDecimal quantity;

    private OrderSide side;

    private OrderType orderType;

    private BigDecimal triggerPrice;



}
