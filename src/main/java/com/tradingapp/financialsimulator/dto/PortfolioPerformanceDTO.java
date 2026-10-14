package com.tradingapp.financialsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioPerformanceDTO {
    private List<PerformanceDataPointDTO> performanceData;
}
