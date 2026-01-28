package com.marketlens.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YahooFinanceService {
    
    @Value("${yahoo.finance.api.base-url}")
    private String baseUrl;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public YahooFinanceService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public List<StockPrice> fetchHistoricalData(String ticker, String period) {
        try {
            String range = convertPeriodToRange(period);
            String url = String.format("%s/v8/finance/chart/%s?interval=1d&range=%s", 
                    baseUrl, ticker, range);
            
            log.info("Fetching data from Yahoo Finance: {}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("Yahoo Finance API returned status: {}", response.statusCode());
                throw new RuntimeException("Failed to fetch data from Yahoo Finance");
            }
            
            return parseYahooFinanceResponse(response.body());
            
        } catch (Exception e) {
            log.error("Error fetching data for ticker: {}", ticker, e);
            throw new RuntimeException("Failed to fetch stock data: " + e.getMessage());
        }
    }
    
    private List<StockPrice> parseYahooFinanceResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode result = root.path("chart").path("result").get(0);
        
        JsonNode timestamps = result.path("timestamp");
        JsonNode quotes = result.path("indicators").path("quote").get(0);
        JsonNode closes = quotes.path("close");
        
        List<StockPrice> prices = new ArrayList<>();
        
        for (int i = 0; i < timestamps.size(); i++) {
            long timestamp = timestamps.get(i).asLong();
            JsonNode closeNode = closes.get(i);
            
            if (!closeNode.isNull()) {
                double close = closeNode.asDouble();
                LocalDate date = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                prices.add(new StockPrice(date.toString(), close));
            }
        }
        
        log.info("Parsed {} price points", prices.size());
        return prices;
    }
    
    private String convertPeriodToRange(String period) {
        return switch (period.toUpperCase()) {
            case "1Y" -> "1y";
            case "6M" -> "6mo";
            case "3M" -> "3mo";
            case "1M" -> "1mo";
            default -> "1y";
        };
    }
    
    public static class StockPrice {
        public String date;
        public double close;
        
        public StockPrice(String date, double close) {
            this.date = date;
            this.close = close;
        }
    }
}