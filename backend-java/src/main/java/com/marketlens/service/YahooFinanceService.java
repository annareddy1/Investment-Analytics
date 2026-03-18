package com.marketlens.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public YahooFinanceService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.objectMapper = objectMapper;
    }

    public List<StockPrice> fetchHistoricalData(String ticker, String period) {
        String range = convertPeriodToRange(period);
        String url = String.format("%s/v8/finance/chart/%s?interval=1d&range=%s", baseUrl, ticker, range);

        log.info("Fetching historical data for ticker {} with range {}", ticker, range);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("Yahoo Finance returned status " + response.statusCode());
            }

            return parseYahooFinanceResponse(response.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch stock data for ticker " + ticker, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while fetching stock data for ticker " + ticker, e);
        }
    }

    private List<StockPrice> parseYahooFinanceResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);

        JsonNode resultArray = root.path("chart").path("result");
        if (!resultArray.isArray() || resultArray.isEmpty()) {
            throw new IllegalArgumentException("No chart data returned from Yahoo Finance");
        }

        JsonNode result = resultArray.get(0);
        JsonNode timestamps = result.path("timestamp");
        JsonNode quotesArray = result.path("indicators").path("quote");

        if (!quotesArray.isArray() || quotesArray.isEmpty()) {
            throw new IllegalArgumentException("No quote data returned from Yahoo Finance");
        }

        JsonNode closes = quotesArray.get(0).path("close");

        List<StockPrice> prices = new ArrayList<>();

        for (int i = 0; i < timestamps.size() && i < closes.size(); i++) {
            JsonNode closeNode = closes.get(i);

            if (closeNode != null && !closeNode.isNull()) {
                long timestamp = timestamps.get(i).asLong();
                double close = closeNode.asDouble();

                LocalDate date = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                prices.add(new StockPrice(date.toString(), close));
            }
        }

        if (prices.isEmpty()) {
            throw new IllegalArgumentException("No valid price data found for response");
        }

        log.info("Parsed {} price points", prices.size());
        return prices;
    }

    private String convertPeriodToRange(String period) {
        return switch (period.toUpperCase()) {
            case "1M" -> "1mo";
            case "3M" -> "3mo";
            case "6M" -> "6mo";
            case "1Y" -> "1y";
            case "5Y" -> "5y";
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        };
    }

    @Getter
    public static class StockPrice {
        private final String date;
        private final double close;

        public StockPrice(String date, double close) {
            this.date = date;
            this.close = close;
        }
    }
}