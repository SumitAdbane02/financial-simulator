package com.tradingapp.financialsimulator.scheduler;

import com.tradingapp.financialsimulator.model.Stock;
import com.tradingapp.financialsimulator.repository.StockRepository;
import com.tradingapp.financialsimulator.service.FinancialDataService;
import com.tradingapp.financialsimulator.service.InfluxDBService;
import com.tradingapp.financialsimulator.service.MarketDataCache;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A scheduled component responsible for periodically fetching and updating stock prices.
 */
@Component // Marks this class as a Spring-managed component.
@RequiredArgsConstructor // Lombok: Injects dependencies via the constructor.
public class StockPriceUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceUpdateScheduler.class);

    // Inject the repositories and services needed to perform the task.
    private final StockRepository stockRepository;
    private final FinancialDataService financialDataService;

   // highlight-start
    // Inject the singleton InfluxDBService bean.
    // Spring will automatically provide the instance we configured in InfluxDBConfig.
    private final InfluxDBService influxDBService;
    private final MarketDataCache marketDataCache;
    /**
     * This method is executed at a fixed rate to update stock prices.
     * The @Scheduled annotation defines when this method runs.
     *  - fixedRate = 60000: This means the method will be executed every 60,000 milliseconds (60 seconds).
     *      The time is measured from the start time of each execution.
     *  - initialDelay = 10000: The first execution will be delayed by 10,000 milliseconds (10 seconds)
     *      after the application has started. This gives the application time to fully initialize.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void updateStockPrices() {
        logger.info("Scheduler starting: Fetching live stock prices...");

        // 1. Get all the stocks we want to track from our database.
        List<Stock> stocks = stockRepository.findAll();

        if (stocks.isEmpty()) {
            logger.warn("Stock price update scheduler ran, but no stocks were found in the database. Exiting.");
            return;
        }

        logger.info("Found {} stocks to update prices for.", stocks.size());

        // 2. Iterate through each stock and fetch its latest quote.
        for (Stock stock : stocks) {
            try {
                // Call the service method we built in the previous task.
                financialDataService.getQuote(stock.getSymbol())
                        .ifPresent(quote -> {
                            // In the next task, we will store this quote in a cache.
                            // For now, we simply log the fetched price to confirm the entire flow is working.

                            marketDataCache.updatePrice(stock.getSymbol(),quote.getCurrentPrice());

                            influxDBService.writePricePoint(stock.getSymbol(),quote.getCurrentPrice());
                            // TODO: Add logic to store/cache the price.
                        });

                // 3. BEST PRACTICE: Add a small delay between API calls to respect rate limits.
                // The Finnhub free tier has a limit of 60 calls/minute. A 1-second delay
                // ensures we stay well within this limit and act as a good API citizen.
                Thread.sleep(1000); // 1-second (1000 milliseconds) delay

            } catch (InterruptedException e) {
                // This exception is thrown if the thread is interrupted while sleeping.
                // It's good practice to restore the interrupted status and log the event.
                Thread.currentThread().interrupt();
                logger.error("Scheduler thread was interrupted while sleeping.", e);
                // Exit the loop if the thread is interrupted.
            } catch (Exception e) {
                // This general catch block ensures that if fetching the price for one stock fails
                // (e.g., due to a temporary API error for that symbol), the entire scheduler
                // doesn't crash. It will log the error and continue to the next stock.
                logger.error("Error updating price for symbol {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        logger.info("Scheduler finished: Current cache state: {}.",marketDataCache.getCacheState());
    }
}