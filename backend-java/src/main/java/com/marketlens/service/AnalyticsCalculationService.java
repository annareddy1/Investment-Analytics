package com.marketlens.service;

import com.marketlens.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AnalyticsCalculationService {
    
    public AnalysisResult.Analytics calculateAnalytics(List<YahooFinanceService.StockPrice> prices) {
        List<Double> priceValues = prices.stream()
                .map(p -> p.close)
                .toList();
        
        // Calculate returns
        List<Double> returns = calculateReturns(priceValues);
        
        // Calculate cumulative return
        double cumulativeReturn = (priceValues.get(priceValues.size() - 1) / priceValues.get(0)) - 1;
        
        // Calculate max drawdown
        double maxDrawdown = calculateMaxDrawdown(priceValues);
        
        // Calculate rolling volatility
        List<Double> volatilityValues = calculateRollingVolatility(returns, 30);
        double latestVolatility = volatilityValues.isEmpty() ? 0.0 : 
                volatilityValues.get(volatilityValues.size() - 1);
        
        // Calculate RSI
        List<Double> rsiValues = calculateRSI(priceValues, 14);
        double latestRSI = rsiValues.isEmpty() ? 50.0 : 
                rsiValues.get(rsiValues.size() - 1);
        
        log.info("Analytics calculated - Return: {}, Drawdown: {}, Volatility: {}, RSI: {}",
                cumulativeReturn, maxDrawdown, latestVolatility, latestRSI);
        
        return new AnalysisResult.Analytics(
                cumulativeReturn,
                maxDrawdown,
                latestVolatility,
                latestRSI
        );
    }
    
    public AnalysisResult.Charts generateCharts(List<YahooFinanceService.StockPrice> prices) {
        List<Double> priceValues = prices.stream()
                .map(p -> p.close)
                .toList();
        
        // Price chart
        List<AnalysisResult.PricePoint> pricePoints = new ArrayList<>();
        for (YahooFinanceService.StockPrice price : prices) {
            pricePoints.add(new AnalysisResult.PricePoint(price.date, price.close));
        }
        
        // Returns chart
        List<Double> returns = calculateReturns(priceValues);
        List<AnalysisResult.ReturnPoint> returnPoints = new ArrayList<>();
        for (int i = 0; i < returns.size(); i++) {
            returnPoints.add(new AnalysisResult.ReturnPoint(
                    prices.get(i + 1).date,
                    returns.get(i) * 100  // Convert to percentage
            ));
        }
        
        // Volatility chart
        List<Double> volatilityValues = calculateRollingVolatility(returns, 30);
        List<AnalysisResult.VolatilityPoint> volatilityPoints = new ArrayList<>();
        for (int i = 0; i < volatilityValues.size(); i++) {
            volatilityPoints.add(new AnalysisResult.VolatilityPoint(
                    prices.get(i + 30).date,
                    volatilityValues.get(i) * 100  // Convert to percentage
            ));
        }
        
        // RSI chart
        List<Double> rsiValues = calculateRSI(priceValues, 14);
        List<AnalysisResult.RsiPoint> rsiPoints = new ArrayList<>();
        for (int i = 0; i < rsiValues.size(); i++) {
            rsiPoints.add(new AnalysisResult.RsiPoint(
                    prices.get(i + 14).date,
                    rsiValues.get(i)
            ));
        }
        
        return new AnalysisResult.Charts(pricePoints, returnPoints, volatilityPoints, rsiPoints);
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
            double annualizedVol = stdDev * Math.sqrt(252);  // Annualize
            volatility.add(annualizedVol);
        }
        
        return volatility;
    }
    
    private List<Double> calculateRSI(List<Double> prices, int period) {
        List<Double> rsi = new ArrayList<>();
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        
        // Calculate price changes
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            gains.add(change > 0 ? change : 0);
            losses.add(change < 0 ? Math.abs(change) : 0);
        }
        
        // Calculate RSI
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
            
            double rsiValue;
            if (avgLoss == 0) {
                rsiValue = 100.0;
            } else {
                double rs = avgGain / avgLoss;
                rsiValue = 100 - (100 / (1 + rs));
            }
            
            rsi.add(rsiValue);
        }
        
        return rsi;
    }
}