package com.tradingapp.financialsimulator.controller;

import com.tradingapp.financialsimulator.dto.HistoricalPricePointDTO;
import com.tradingapp.financialsimulator.service.InfluxDBService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    private final InfluxDBService influxDBService;

    @GetMapping("/{symbol}/history")
    public ResponseEntity<List<HistoricalPricePointDTO>> getStockHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d")String range
    ){
        if (!symbol.matches("^[A-Z]{1,5}$")){
            return ResponseEntity.badRequest().build();
        }
        List<HistoricalPricePointDTO> history=influxDBService.getHistoricalPriceData(symbol.toUpperCase(),range);
        return ResponseEntity.ok(history);

    }


}
