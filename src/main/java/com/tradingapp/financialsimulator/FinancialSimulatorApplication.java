package com.tradingapp.financialsimulator;

import com.tradingapp.financialsimulator.model.Stock;
import com.tradingapp.financialsimulator.repository.StockRepository;
import com.tradingapp.financialsimulator.service.FinancialDataService; // Import the service
import org.springframework.boot.CommandLineRunner; // Import CommandLineRunner
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // Import Bean
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class FinancialSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialSimulatorApplication.class, args);
	}

	// highlight-start
	/**
	 * A temporary bean to test the FinancialDataService on application startup.
	 * This runner will be executed once the Spring context is loaded.
	 * It's a great way to test service-layer logic without needing a controller.
	 *
	 * @param financialDataService The service we want to test, injected by Spring.
	 * @return A CommandLineRunner instance.
	 */
	@Bean
	public CommandLineRunner testFinancialDataService(FinancialDataService financialDataService) {
		return args -> {
			System.out.println("--- TESTING FINANCIAL DATA SERVICE ---");
			String testSymbol = "AAPL"; // Apple Inc.
			financialDataService.getQuote(testSymbol).ifPresent(quote -> {
				System.out.println("Quote for " + testSymbol + ": " + quote);
			});

			String invalidSymbol = "FAKESYMBOL123";
			financialDataService.getQuote(invalidSymbol).ifPresentOrElse(
					quote -> System.out.println("Quote for " + invalidSymbol + ": " + quote),
					() -> System.out.println("No quote found for " + invalidSymbol + ", as expected.")
			);
			System.out.println("--- TEST COMPLETE ---");
		};
		// highlight-end
	}

	@Bean
	public CommandLineRunner seedDatabase(StockRepository stockRepository) {
		return args -> {
			System.out.println("--- PRE-POPULATING STOCK DATA ---");

			List<Stock> initialStocks = Arrays.asList(
					Stock.builder().symbol("AAPL").name("Apple Inc.").exchange("NASDAQ").build(),
					Stock.builder().symbol("GOOGL").name("Alphabet Inc.").exchange("NASDAQ").build(),
					Stock.builder().symbol("TSLA").name("Tesla, Inc.").exchange("NASDAQ").build(),
					Stock.builder().symbol("MSFT").name("Microsoft Corporation").exchange("NASDAQ").build()
			);

			for (Stock stock : initialStocks) {
				// Check if the stock already exists by its unique symbol
				if (stockRepository.findBySymbol(stock.getSymbol()).isEmpty()) {
					stockRepository.save(stock);
					System.out.println("Saved stock: " + stock.getSymbol());
				}
			}
			System.out.println("--- STOCK DATA POPULATION COMPLETE ---");
		};
	}

}