// highlight-start
package com.tradingapp.financialsimulator.service;

import com.tradingapp.financialsimulator.dto.Quote;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinancialDataService {

    private static final String FINNHUB_API_BASE_URL = "https://finnhub.io/api/v1";
    // A Logger is a standard way to output informational messages and errors.
    private static final Logger logger = LoggerFactory.getLogger(FinancialDataService.class);

    private final RestTemplate restTemplate;

    @Value("${finnhub.api.key}")
    private String apiKey;

    /**
     * Fetches the latest quote for a given stock symbol from the Finnhub API.
     *
     * @param symbol The stock symbol (e.g., "AAPL") to fetch the quote for.
     * @return An Optional containing the Quote object if the API call is successful
     *         and the price is not zero, otherwise an empty Optional.
     */

    @Retryable(
            value = { RestClientException.class},
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2
            )
    )
    public Optional<Quote> getQuote(String symbol) {
        logger.info("Requesting quote for symbol: {}", symbol);

        // Build the URL with query parameters in a safe way using UriComponentsBuilder.
        // This handles URL encoding and makes the code cleaner than manual string concatenation.
        String url = UriComponentsBuilder.fromHttpUrl(FINNHUB_API_BASE_URL)
                .path("/quote")
                .queryParam("symbol", symbol)
                .queryParam("token", apiKey)
                .toUriString();


            // Make the GET request to the Finnhub API.
            // RestTemplate will automatically deserialize the JSON response into our Quote object.
            Quote quote = restTemplate.getForObject(url, Quote.class);

            // The Finnhub API sometimes returns a successful response with all-zero values
            // for invalid symbols. We treat this as a "not found" case.
            if (quote != null && quote.getCurrentPrice() != null && quote.getCurrentPrice().signum() != 0) {
                logger.info("Successfully fetched quote for {}: {}", symbol, quote);
                return Optional.of(quote);
            } else {
                logger.warn("Received null or zero-price quote for symbol: {}. Treating as not found.", symbol);
                return Optional.empty();
            }



    }

    public Optional<Quote> recover(RestClientException e,String symbol){
        logger.error("Failed to fetch quote for symbol '{}' after multiple retries. Final error: {}",symbol,e.getMessage());

        if (e instanceof HttpClientErrorException){
            HttpClientErrorException hce=(HttpClientErrorException) e;
            if (hce.getStatusCode().value()==429){
                logger.error("Rate limit exceeded for Finnhub API Consider increasing the delay in the schedular ");

            } else if (hce.getStatusCode().is4xxClientError()) {
                logger.error("Client error: {} -check your API key or the requested symbol {} .",hce.getStatusCode(),symbol);


            }
        }
        return Optional.empty();
    }
}
// highlight-end