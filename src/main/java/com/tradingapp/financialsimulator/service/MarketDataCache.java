package com.tradingapp.financialsimulator.service;

import org.springframework.stereotype.Service;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataCache {

    private final Map<String, BigDecimal> priceCache=new ConcurrentHashMap<>();


    public void updatePrice(String symbol,BigDecimal price){
        priceCache.put(symbol, price);
    }

    public Optional<BigDecimal> getPrice(String symbol){

        return Optional.ofNullable(priceCache.get(symbol));
    }

    public Map<String,BigDecimal> getCacheState(){
        return Map.copyOf(priceCache);
    }
}
