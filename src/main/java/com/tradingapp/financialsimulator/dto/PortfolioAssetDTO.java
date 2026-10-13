package com.tradingapp.financialsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAssetDTO {
    private String stockSymbol;
    private String stockName;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;


}
