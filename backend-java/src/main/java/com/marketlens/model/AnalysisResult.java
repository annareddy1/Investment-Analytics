package com.marketlens.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "analysis_results")
@Getter
@Setter
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Analytics {
        private Double cumulativeReturn;
        private Double maxDrawdown;
        private Double latestVolatility;
        private Double latestRSI;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Charts {
        private List<PricePoint> prices;
        private List<ReturnPoint> returns;
        private List<VolatilityPoint> volatility;
        private List<RsiPoint> rsi;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private String date;
        private Double price;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnPoint {
        private String date;
        private Double returnValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolatilityPoint {
        private String date;
        private Double volatility;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RsiPoint {
        private String date;
        private Double rsi;
    }
}