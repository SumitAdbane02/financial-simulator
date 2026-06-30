package com.tradingapp.financialsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalPricePointDTO {

    // The timestamp for this data point. Instant is used for a point-in-time UTC value.
    private Instant time;

    // The price at the given time.
    private BigDecimal price;

}
