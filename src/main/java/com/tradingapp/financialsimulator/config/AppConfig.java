package com.tradingapp.financialsimulator.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableRetry   // This annotation enables Spring's retry functionality.
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){

        // The RestTemplateBuilder allows for easy customization of the RestTemplate,
        // such as setting connection timeouts, read timeouts, or adding interceptors.
        // For now, we will use its default configuration.
        return builder.build();
    }
}
