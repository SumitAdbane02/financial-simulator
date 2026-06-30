package com.tradingapp.financialsimulator.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration // Marks this class as a source of Spring bean definitions.
public class InfluxDBConfig {

    // Injecting connection details from application.properties
    @Value("${influxdb.url}")
    private String influxUrl;

    @Value("${influxdb.token}")
    private String influxToken;

    @Value("${influxdb.org}")
    private String influxOrg;


    @Bean
    public InfluxDBClient influxDBClient() {
        // InfluxDBClientFactory is the designated way to create a client instance.
        // We pass the credentials injected from our properties file.
        // Using token.toCharArray() is a security best practice over passing a String directly.
        return InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg);
    }
}