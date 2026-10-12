package com.tradingapp.financialsimulator.dto;

import com.tradingapp.financialsimulator.model.OrderStatus;
import com.tradingapp.financialsimulator.model.OrderType;
import com.tradingapp.financialsimulator.model.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {

    private Long id;
    private String stockSymbol;
    private OrderType orderType;
    private OrderSide side;
    private OrderStatus orderStatus;
    private BigDecimal quantity;

    private LocalDateTime createAt;

    private BigDecimal triggerPrice;

}
