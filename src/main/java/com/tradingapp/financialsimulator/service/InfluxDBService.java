package com.tradingapp.financialsimulator.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.tradingapp.financialsimulator.dto.HistoricalPricePointDTO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for all interactions with InfluxDB.
 * This class encapsulates the logic for writing data points to the time-series database.
 */
@Service
@RequiredArgsConstructor
public class InfluxDBService {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBService.class);

    // Inject the centrally-configured InfluxDB client bean.
    private final InfluxDBClient influxDBClient;
    @Value("${influxdb.org}")
    private String influxOrg;
    // Inject the bucket name from properties, as it's needed for every write operation.
    @Value("${influxdb.bucket}")
    private String influxBucket;


    public void writePricePoint(String stockSymbol, BigDecimal price) {
        try {
            // A 'Point' is the fundamental data structure in InfluxDB.
            // It represents a single data point at a specific moment in time.
            Point point = Point
                    // 1. Measurement: Acts like a table name in a relational database.
                    .measurement("stock_price")
                    // 2. Tag: A key-value pair that is indexed and used for fast queries (e.g., filtering by symbol).
                    .addTag("symbol", stockSymbol)
                    // 3. Field: The actual data value. Fields are not indexed in the same way as tags.
                    .addField("price", price)
                    // 4. Timestamp: The time associated with the data point. We use the current time.
                    .time(Instant.now(), WritePrecision.NS);

            // The WriteApi is the component used to send data to InfluxDB.
            // Using a try-with-resources block ensures the API is properly closed after use.
            try (WriteApi writeApi = influxDBClient.getWriteApi()) {
                writeApi.writePoint(influxBucket, influxOrg, point);
                logger.debug("Successfully wrote data point for {} to InfluxDB.", stockSymbol);
            }

        } catch (Exception e) {
            // A robust catch block to prevent any InfluxDB issue from crashing the calling process (e.g., the scheduler).
            logger.error("Error writing data point to InfluxDB for symbol {}: {}", stockSymbol, e.getMessage());
        }
    }

    public List<HistoricalPricePointDTO> getHistoricalPriceData(String symbol,String range){

        String fluxQuery=String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -%s) " +
                        "|> filter(fn: (r) => r._measurement == \"stock_price\") " +
                        "|> filter(fn: (r) => r._field == \"price\") " +
                        "|> filter(fn: (r) => r.symbol == \"%s\")",
                influxBucket, range, symbol
        );
        logger.info("Executing Flux query: {}",fluxQuery);
       List<HistoricalPricePointDTO> historicalData=new ArrayList<>();
        QueryApi queryApi=influxDBClient.getQueryApi();
        try {
            List<FluxTable> tables=queryApi.query(fluxQuery);

            for (FluxTable table:tables){
                for (FluxRecord record: table.getRecords()){
                    Instant time=record.getTime();
                    Object value=record.getValue();

                    if (time != null && value != null) {

                        BigDecimal price=new BigDecimal(value.toString());
                        historicalData.add(new HistoricalPricePointDTO(time,price));

                    }
                }
            }
        }catch (Exception e){
            logger.error("Error querying InfluxDB for historical data for symbol '{}': {}", symbol, e.getMessage());
            // Return an empty list in case of an error to prevent crashing the caller.
            return List.of();
        }

        logger.info("Found {} historical data points for symbol '{}' in range '{}'", historicalData.size(), symbol, range);
        return historicalData;
    }
}