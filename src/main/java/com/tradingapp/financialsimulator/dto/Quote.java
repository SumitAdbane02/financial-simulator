package com.tradingapp.financialsimulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A Data Transfer Object (DTO) that represents the quote data received from the Finnhub API.
 * The @JsonProperty annotation is used to map the JSON fields from the API response
 * (which have short, cryptic names) to our more readable Java field names.
 */
@Data // Lombok: Generates getters, setters, toString(), etc.
public class Quote {

    // "c" -> Current price
    @JsonProperty("c")
    private BigDecimal currentPrice;

    // "d" -> Change
    @JsonProperty("d")
    private BigDecimal change;

    // "dp" -> Percent change
    @JsonProperty("dp")
    private BigDecimal percentChange;

    // "h" -> High price of the day
    @JsonProperty("h")
    private BigDecimal highPrice;

    // "l" -> Low price of the day
    @JsonProperty("l")
    private BigDecimal lowPrice;

      // "o" -> Open price of the day
    @JsonProperty("o")
    private BigDecimal openPrice;

    // "pc" -> Previous close price
    @JsonProperty("pc")
    private BigDecimal previousClosePrice;

    // "t" -> Timestamp (we can ignore this for now if not needed)
    @JsonProperty("t")
    private long timestamp;
}