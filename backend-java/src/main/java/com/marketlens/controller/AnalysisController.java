package com.marketlens.controller;

import com.marketlens.dto.AnalysisRequest;
import com.marketlens.dto.AnalysisResponse;
import com.marketlens.dto.AnalysisResultResponse;
import com.marketlens.dto.AnalysisStatusResponse;
import com.marketlens.model.AnalysisResult;
import com.marketlens.model.AnalysisRun;
import com.marketlens.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/run")
    public ResponseEntity<AnalysisResponse> runAnalysis(@Valid @RequestBody AnalysisRequest request) {
        AnalysisRun run = analysisService.createAnalysisRun(
                request.getTicker(),
                request.getPeriod()
        );

        analysisService.processAnalysis(run.getId());

        AnalysisResponse response = AnalysisResponse.builder()
                .analysisId(run.getId().toString())
                .ticker(run.getTicker())
                .status(run.getStatus().name())
                .message("Analysis job started successfully")
                .progress(run.getProgress())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{analysisId}/status")
    public ResponseEntity<AnalysisStatusResponse> getAnalysisStatus(@PathVariable UUID analysisId) {
        AnalysisRun run = analysisService.getAnalysisRun(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));

        return ResponseEntity.ok(buildStatusResponse(run));
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getAnalysis(@PathVariable UUID analysisId) {
        AnalysisRun run = analysisService.getAnalysisRun(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));

        if (run.getStatus() == AnalysisRun.AnalysisStatus.PROCESSING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(buildStatusResponse(run));
        }

        if (run.getStatus() == AnalysisRun.AnalysisStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildStatusResponse(run));
        }

        AnalysisResult result = analysisService.getAnalysisResult(analysisId.toString())
                .orElseThrow(() -> new IllegalStateException("Completed analysis result not found"));

        return ResponseEntity.ok(buildCompletedResponse(result, analysisId.toString()));
    }

    @GetMapping("/ticker/{ticker}/latest")
    public ResponseEntity<AnalysisResultResponse> getLatestAnalysis(@PathVariable String ticker) {
        AnalysisRun run = analysisService.getLatestCompletedAnalysis(ticker)
                .orElseThrow(() -> new IllegalArgumentException("No completed analysis found for ticker: " + ticker));

        AnalysisResult result = analysisService.getAnalysisResult(run.getId().toString())
                .orElseThrow(() -> new IllegalStateException("Completed analysis result not found"));

        return ResponseEntity.ok(buildCompletedResponse(result, run.getId().toString()));
    }

    private AnalysisStatusResponse buildStatusResponse(AnalysisRun run) {
        return AnalysisStatusResponse.builder()
                .analysisId(run.getId().toString())
                .ticker(run.getTicker())
                .period(run.getPeriod())
                .status(run.getStatus().name())
                .progress(run.getProgress())
                .message(getStatusMessage(run))
                .createdAt(run.getCreatedAt())
                .updatedAt(run.getUpdatedAt())
                .completedAt(run.getCompletedAt())
                .errorMessage(run.getErrorMessage())
                .build();
    }

    private String getStatusMessage(AnalysisRun run) {
        return switch (run.getStatus()) {
            case PROCESSING -> "Analysis is in progress";
            case COMPLETED -> "Analysis completed successfully";
            case FAILED -> "Analysis failed";
        };
    }

    private AnalysisResultResponse buildCompletedResponse(AnalysisResult result, String analysisId) {
        return AnalysisResultResponse.builder()
                .analysisId(analysisId)
                .ticker(result.getTicker())
                .status("COMPLETED")
                .period(result.getPeriod())
                .generatedAt(result.getGeneratedAt())
                .analytics(result.getAnalytics())
                .charts(result.getCharts())
                .build();
    }
}