package com.tradingapp.financialsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioDetailsDTO {

    private BigDecimal cashBalances;
    private BigDecimal assetsMarketValue;
    private BigDecimal totalPortfolioValue;
    private List<PortfolioAssetViewDTO> assets;

    // Total original cost
    private BigDecimal totalAssetsCostBasis;
    // Total profit or loss across all assets

    private BigDecimal totalUnrealizedProfitLoss;


}
