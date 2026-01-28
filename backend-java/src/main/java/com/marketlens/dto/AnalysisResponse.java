package com.marketlens.dto;

import com.marketlens.model.AnalysisRun;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponse {
    private String analysisId;
    private String ticker;
    private String status;
    private String message;
    private Integer progress;
}