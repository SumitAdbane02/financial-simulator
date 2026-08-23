package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Portfolio;
import com.tradingapp.financialsimulator.model.PortfolioAsset;
import com.tradingapp.financialsimulator.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset,Long> {

    Optional<PortfolioAsset> findByPortfolioAndStock(Portfolio portfolio, Stock stock);

}
