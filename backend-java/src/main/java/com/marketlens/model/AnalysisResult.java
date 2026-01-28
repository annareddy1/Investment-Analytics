package com.marketlens.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "analysis_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    
    @Id
    private String id;
    
    private String analysisId;
    private String ticker;
    private String period;
    private LocalDateTime generatedAt;
    
    private Analytics analytics;
    private Charts charts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Analytics {
        private Double cumulativeReturn;
        private Double maxDrawdown;
        private Double latestVolatility;
        private Double latestRSI;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Charts {
        private List<PricePoint> prices;
        private List<ReturnPoint> returns;
        private List<VolatilityPoint> volatility;
        private List<RsiPoint> rsi;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private String date;
        private Double price;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnPoint {
        private String date;
        private Double returnValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolatilityPoint {
        private String date;
        private Double volatility;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RsiPoint {
        private String date;
        private Double rsi;
    }
}