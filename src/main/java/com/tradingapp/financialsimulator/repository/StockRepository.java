package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {
    Optional<Stock> findBySymbol(String symbol);
}
