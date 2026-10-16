package com.tradingapp.financialsimulator.dto;

import com.tradingapp.financialsimulator.model.PortfolioAsset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PortfolioUpdateDTO {

    private BigDecimal cashBalance;
    private List<PortfolioAssetDTO> assets;


}
