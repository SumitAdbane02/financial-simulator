package com.tradingapp.financialsimulator.service;

import com.tradingapp.financialsimulator.dto.*;
import com.tradingapp.financialsimulator.model.*;
import com.tradingapp.financialsimulator.repository.PortfolioAssetRepository;
import com.tradingapp.financialsimulator.repository.TransactionRepository;
import com.tradingapp.financialsimulator.repository.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private static final Logger logger= LoggerFactory.getLogger(PortfolioService.class);

    private final UserRepository userRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;

    private final MarketDataCache marketDataCache;

    private final TransactionRepository transactionRepository;
    private final InfluxDBService influxDBService;


    @Cacheable(cacheNames = "portfolioPerformance")
    public PortfolioPerformanceDTO getPortfolioPerformance(String username,int days){
        logger.info("Calculating portfolio performances for user ",username,days);

        // Fetch all necessary historical data
        User user=userRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("User not found"+username));

        List<Transaction> allTransaction=transactionRepository.findByPortfolioOrderByTimestampAsc( user.getPortfolio());

        LocalDate endDate=LocalDate.now();
        LocalDate startDate=endDate.minusDays(days);

        // Fetch all uniques stock the user has ever traded
        Set<String> allTrandedSymbols=allTransaction.stream()
                .map(t->t.getStock().getSymbol())
                .collect(Collectors.toSet());

        // Fetch all historical prices we need in one go form InfluxDB
        Map<String,List<PerformanceDataPointDTO>> priceHistoryMap=influxDBService
                .getHistoricalDailyDataForSymbols
                        (new ArrayList<>(allTrandedSymbols),startDate,endDate);

        List<PerformanceDataPointDTO> performanceData=new ArrayList<>();
        Map<String, BigDecimal> currentHoldings=new HashMap<>();

        BigDecimal currentCash=new BigDecimal(100000.00);
        int transactionIndex=0;

        // iterate day-by-day from the start date to today
        for (LocalDate date=startDate; !date.isAfter(endDate); date=date.plusDays(1)){

            // This loop ensures we process all transaction that happened on or before the current days
            while (transactionIndex<allTransaction.size() &&
                    !allTransaction.get(transactionIndex).getTimestamp().toLocalDate().isAfter(date)){

                Transaction tx=allTransaction.get(transactionIndex);
                String symbol=tx.getStock().getSymbol();
                BigDecimal quantity=tx.getQuantity();
                BigDecimal totalValue=tx.getPrice().multiply(quantity);

                if (tx.getType()== TransactionType.BUY){
                    currentHoldings.put(symbol,currentHoldings.getOrDefault (symbol,BigDecimal.ZERO).add(quantity));
                    currentCash=currentCash.subtract(totalValue);
                }else {
                    currentHoldings.put(symbol,currentHoldings.getOrDefault(symbol,BigDecimal.ZERO).add(quantity));
                    currentCash=currentCash.subtract(totalValue);
                }
                transactionIndex++;
            }

            //calculate portfolio value for the current date
            BigDecimal assetsValueOnDate=BigDecimal.ZERO;

            for (Map.Entry<String,BigDecimal> entry:currentHoldings.entrySet()){

                String symbol=entry.getKey();
                BigDecimal quantity=entry.getValue();


                // Find the price for this stock
                BigDecimal priceOnDate=findPriceForDate(priceHistoryMap.get(symbol),date);
                assetsValueOnDate=assetsValueOnDate.add(priceOnDate.multiply(quantity));

            }

            BigDecimal totalValueOnDate=currentCash.add(assetsValueOnDate);
            performanceData.add(new PerformanceDataPointDTO(date,totalValueOnDate));

        }

        return new PortfolioPerformanceDTO(performanceData);
    }

    private BigDecimal findPriceForDate(List<PerformanceDataPointDTO> priceHistory, LocalDate date) {
        if (priceHistory==null || priceHistory.isEmpty()){
            return BigDecimal.ZERO;
        }
        // This is simplified the search
        for (int i=priceHistory.size()-1;i>=0;i--){
            PerformanceDataPointDTO point=priceHistory.get(i);
            if (!point.getDate().isAfter(date)){
                return point.getValue();
            }
        }
        return BigDecimal.ZERO; // return ) if no price is found on or before the  date
    }

    public PortfolioDetailsDTO getPortfolioDetails(String username) {
        logger.info("Fetching the portfolio details for user {} " + username);


        // Find user and their associated details
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found " + username));

        Portfolio portfolio = user.getPortfolio();

        // find all user asset holding for database
        List<PortfolioAsset> assets = portfolioAssetRepository.findByPortfolio(portfolio);

        // This list will hold our new,enriched asset DTO
        List<PortfolioAssetViewDTO> assetViews = new ArrayList<>();

        // Initialize a big decimal for calculating the total value.
        BigDecimal totalAssetsValue = BigDecimal.ZERO;

        // Track of the total cost basis
        BigDecimal totalAssetsCostBasis=BigDecimal.ZERO;

        for (PortfolioAsset asset : assets) {
            String symbol = asset.getStock().getSymbol();

            BigDecimal currentPrice = marketDataCache.getPrice(symbol).orElse(BigDecimal.ZERO);
            BigDecimal currentValue = currentPrice.multiply(asset.getQuantity());

            // Calculate the cost basis, User paid for assets
            BigDecimal costBasis=asset.getAverageBuyPrice().multiply(asset.getQuantity());

            // P/L=current Value -Original cost
            BigDecimal profitLoss=currentValue.subtract(costBasis);

            // Add this asset cost to the runnig total
            totalAssetsCostBasis=totalAssetsCostBasis.add(costBasis);


            // Build the enriched DTO for this asset
            assetViews.add(PortfolioAssetViewDTO.builder()
                    .stockSymbol(symbol)
                    .stockName(asset.getStock().getName())
                    .quantity(asset.getQuantity())
                    .averageBuyPrice(asset.getAverageBuyPrice())
                    .currentPrice(currentPrice)
                    .currentValue(currentValue)
                            .unrealizedProfitLoss(profitLoss)
                    .build());
            // Add this holding's value to our running total
            totalAssetsValue = totalAssetsValue.add(currentValue);
        }

        BigDecimal totalPortfolioValue = portfolio.getCashBalance().add(totalAssetsValue);

        BigDecimal totalProfitLoss=totalAssetsValue.subtract(totalAssetsCostBasis);
        // Final comprehensive DTO to return to the controller
        return PortfolioDetailsDTO.builder()
                .cashBalances(portfolio.getCashBalance())
                .assetsMarketValue(totalAssetsValue)
                .totalPortfolioValue(totalPortfolioValue)
                .totalAssetsCostBasis(totalAssetsCostBasis)
                .totalUnrealizedProfitLoss(totalProfitLoss)
                .assets(assetViews)
                .build();
    }


}
