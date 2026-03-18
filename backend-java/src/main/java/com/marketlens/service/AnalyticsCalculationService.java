package com.marketlens.service;

import com.marketlens.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AnalyticsCalculationService {

    private static final int VOLATILITY_WINDOW = 30;
    private static final int RSI_PERIOD = 14;
    private static final int TRADING_DAYS_PER_YEAR = 252;

    public AnalysisResult.Analytics calculateAnalytics(List<YahooFinanceService.StockPrice> prices) {
        validatePrices(prices);

        List<Double> priceValues = prices.stream()
                .map(YahooFinanceService.StockPrice::getClose)
                .toList();

        List<Double> returns = calculateReturns(priceValues);

        double cumulativeReturn = calculateCumulativeReturn(priceValues);
        double maxDrawdown = calculateMaxDrawdown(priceValues);

        List<Double> volatilityValues = calculateRollingVolatility(returns, VOLATILITY_WINDOW);
        double latestVolatility = volatilityValues.isEmpty()
                ? 0.0
                : volatilityValues.get(volatilityValues.size() - 1);

        List<Double> rsiValues = calculateRSI(priceValues, RSI_PERIOD);
        double latestRSI = rsiValues.isEmpty()
                ? 50.0
                : rsiValues.get(rsiValues.size() - 1);

        log.info("Calculated analytics for {} price points", prices.size());

        return new AnalysisResult.Analytics(
                cumulativeReturn,
                maxDrawdown,
                latestVolatility,
                latestRSI
        );
    }

    public AnalysisResult.Charts generateCharts(List<YahooFinanceService.StockPrice> prices) {
        validatePrices(prices);

        List<Double> priceValues = prices.stream()
                .map(YahooFinanceService.StockPrice::getClose)
                .toList();

        List<AnalysisResult.PricePoint> pricePoints = buildPricePoints(prices);

        List<Double> returns = calculateReturns(priceValues);
        List<AnalysisResult.ReturnPoint> returnPoints = buildReturnPoints(prices, returns);

        List<Double> volatilityValues = calculateRollingVolatility(returns, VOLATILITY_WINDOW);
        List<AnalysisResult.VolatilityPoint> volatilityPoints = buildVolatilityPoints(prices, volatilityValues);

        List<Double> rsiValues = calculateRSI(priceValues, RSI_PERIOD);
        List<AnalysisResult.RsiPoint> rsiPoints = buildRsiPoints(prices, rsiValues);

        return new AnalysisResult.Charts(pricePoints, returnPoints, volatilityPoints, rsiPoints);
    }

    private void validatePrices(List<YahooFinanceService.StockPrice> prices) {
        if (prices == null || prices.size() < 2) {
            throw new IllegalArgumentException("At least two price points are required");
        }
    }

    private double calculateCumulativeReturn(List<Double> prices) {
        return (prices.get(prices.size() - 1) / prices.get(0)) - 1;
    }

    private List<AnalysisResult.PricePoint> buildPricePoints(List<YahooFinanceService.StockPrice> prices) {
        List<AnalysisResult.PricePoint> pricePoints = new ArrayList<>();

        for (YahooFinanceService.StockPrice price : prices) {
            pricePoints.add(new AnalysisResult.PricePoint(price.getDate(), price.getClose()));
        }

        return pricePoints;
    }

    private List<AnalysisResult.ReturnPoint> buildReturnPoints(
            List<YahooFinanceService.StockPrice> prices,
            List<Double> returns
    ) {
        List<AnalysisResult.ReturnPoint> returnPoints = new ArrayList<>();

        for (int i = 0; i < returns.size(); i++) {
            returnPoints.add(new AnalysisResult.ReturnPoint(
                    prices.get(i + 1).getDate(),
                    returns.get(i) * 100
            ));
        }

        return returnPoints;
    }

    private List<AnalysisResult.VolatilityPoint> buildVolatilityPoints(
            List<YahooFinanceService.StockPrice> prices,
            List<Double> volatilityValues
    ) {
        List<AnalysisResult.VolatilityPoint> volatilityPoints = new ArrayList<>();

        for (int i = 0; i < volatilityValues.size(); i++) {
            volatilityPoints.add(new AnalysisResult.VolatilityPoint(
                    prices.get(i + VOLATILITY_WINDOW).getDate(),
                    volatilityValues.get(i) * 100
            ));
        }

        return volatilityPoints;
    }

    private List<AnalysisResult.RsiPoint> buildRsiPoints(
            List<YahooFinanceService.StockPrice> prices,
            List<Double> rsiValues
    ) {
        List<AnalysisResult.RsiPoint> rsiPoints = new ArrayList<>();

        for (int i = 0; i < rsiValues.size(); i++) {
            rsiPoints.add(new AnalysisResult.RsiPoint(
                    prices.get(i + RSI_PERIOD).getDate(),
                    rsiValues.get(i)
            ));
        }

        return rsiPoints;
    }

    private List<Double> calculateReturns(List<Double> prices) {
        List<Double> returns = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double dailyReturn = (prices.get(i) / prices.get(i - 1)) - 1;
            returns.add(dailyReturn);
        }

        return returns;
    }

    private double calculateMaxDrawdown(List<Double> prices) {
        double maxDrawdown = 0.0;
        double peak = prices.get(0);

        for (double price : prices) {
            if (price > peak) {
                peak = price;
            }

            double drawdown = (price / peak) - 1;

            if (drawdown < maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown;
    }

    private List<Double> calculateRollingVolatility(List<Double> returns, int window) {
        List<Double> volatility = new ArrayList<>();

        for (int i = window; i < returns.size(); i++) {
            List<Double> windowReturns = returns.subList(i - window, i);

            double mean = windowReturns.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            double variance = windowReturns.stream()
                    .mapToDouble(r -> Math.pow(r - mean, 2))
                    .sum() / window;

            double stdDev = Math.sqrt(variance);
            double annualizedVolatility = stdDev * Math.sqrt(TRADING_DAYS_PER_YEAR);

            volatility.add(annualizedVolatility);
        }

        return volatility;
    }

    private List<Double> calculateRSI(List<Double> prices, int period) {
        List<Double> rsiValues = new ArrayList<>();
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            gains.add(change > 0 ? change : 0);
            losses.add(change < 0 ? Math.abs(change) : 0);
        }

        for (int i = period; i < gains.size(); i++) {
            List<Double> periodGains = gains.subList(i - period, i);
            List<Double> periodLosses = losses.subList(i - period, i);

            double avgGain = periodGains.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            double avgLoss = periodLosses.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            double rsi;
            if (avgLoss == 0) {
                rsi = 100.0;
            } else {
                double relativeStrength = avgGain / avgLoss;
                rsi = 100 - (100 / (1 + relativeStrength));
            }

            rsiValues.add(rsi);
        }

        return rsiValues;
    }
}