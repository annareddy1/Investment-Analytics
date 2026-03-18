package com.marketlens.dto;

import com.marketlens.model.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponse {

    private String analysisId;
    private String ticker;
    private String status;
    private String period;
    private LocalDateTime generatedAt;
    private AnalysisResult.Analytics analytics;
    private AnalysisResult.Charts charts;
}