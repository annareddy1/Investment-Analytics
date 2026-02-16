package com.marketlens.controller;

import com.marketlens.dto.AnalysisRequest;
import com.marketlens.dto.AnalysisResponse;
import com.marketlens.dto.AnalysisStatusResponse;
import com.marketlens.logging.MdcKeys;
import com.marketlens.model.AnalysisResult;
import com.marketlens.model.AnalysisRun;
import com.marketlens.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ✅ Analysis Controller - Robust Async Job Polling API
 *
 * Endpoints:
 * - POST /api/analysis/run - Start new analysis job
 * - GET /api/analysis/{id}/status - Poll job status (always 200 if exists)
 * - GET /api/analysis/{id} - Get analysis result (202/200/500 based on status)
 * - GET /api/analysis/ticker/{ticker}/latest - Get latest completed analysis
 *
 * Thread Safety:
 * - AnalysisRun stored in PostgreSQL (thread-safe via Spring Data JPA transactions)
 * - Repository operations are atomic and isolated
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * Start an analysis job (async)
     * POST /api/analysis/run
     *
     * Returns: 201 CREATED with job details
     */
    @PostMapping("/run")
    public ResponseEntity<AnalysisResponse> runAnalysis(@Valid @RequestBody AnalysisRequest request) {
        log.info("Starting analysis for ticker={}, period={}", request.getTicker(), request.getPeriod());

        try {
            // Create analysis run (persisted in PostgreSQL - thread-safe)
            AnalysisRun analysisRun = analysisService.createAnalysisRun(
                    request.getTicker(),
                    request.getPeriod()
            );

            // ✅ Add analysis context to MDC for correlation (propagated to async thread via MdcTaskDecorator)
            MDC.put(MdcKeys.ANALYSIS_ID, analysisRun.getId().toString());
            MDC.put(MdcKeys.TICKER, analysisRun.getTicker());
            MDC.put(MdcKeys.PERIOD, analysisRun.getPeriod());

            log.info("Analysis job created - starting async processing");

            // Start async processing (non-blocking)
            analysisService.processAnalysis(analysisRun.getId());

            // Return immediate response with job ID
            AnalysisResponse response = AnalysisResponse.builder()
                    .analysisId(analysisRun.getId().toString())
                    .ticker(analysisRun.getTicker())
                    .status(analysisRun.getStatus().name())
                    .message("Analysis job started. Use /api/analysis/" + analysisRun.getId() + "/status to poll.")
                    .progress(0)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Failed to start analysis for ticker={}", request.getTicker(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AnalysisResponse.builder()
                            .message("Failed to start analysis: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get analysis job status (polling endpoint)
     * GET /api/analysis/{analysisId}/status
     *
     * Returns:
     * - 200 OK with status details (if job exists)
     * - 404 NOT FOUND (if job doesn't exist)
     *
     * This endpoint ALWAYS returns 200 if the job exists, regardless of job state.
     * Status field indicates: PROCESSING, COMPLETED, or FAILED
     */
    @GetMapping("/{analysisId}/status")
    public ResponseEntity<AnalysisStatusResponse> getAnalysisStatus(@PathVariable String analysisId) {
        log.debug("Polling status for analysisId={}", analysisId);

        try {
            UUID uuid = UUID.fromString(analysisId);

            AnalysisRun analysisRun = analysisService.getAnalysisRun(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("Analysis job not found: " + analysisId));

            AnalysisStatusResponse response = buildStatusResponse(analysisRun);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Invalid UUID format or job not found
            log.warn("Invalid analysisId or job not found: {}", analysisId);
            throw new IllegalArgumentException("Invalid or unknown analysis ID");
        }
    }

    /**
     * Get analysis result (blocking endpoint)
     * GET /api/analysis/{analysisId}
     *
     * Returns:
     * - 202 ACCEPTED (still processing) - with status details
     * - 200 OK (completed) - with full analysis results
     * - 500 INTERNAL SERVER ERROR (failed) - with error details
     * - 404 NOT FOUND (job doesn't exist)
     *
     * HTTP Status Code indicates job state:
     * - 202: Client should continue polling
     * - 200: Results are ready
     * - 500: Job failed permanently
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getAnalysis(@PathVariable String analysisId) {
        log.info("Fetching analysis result for analysisId={}", analysisId);

        try {
            UUID uuid = UUID.fromString(analysisId);

            AnalysisRun analysisRun = analysisService.getAnalysisRun(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("Analysis job not found: " + analysisId));

            switch (analysisRun.getStatus()) {
                case PROCESSING:
                    // 202 ACCEPTED - Job still processing
                    AnalysisStatusResponse processingResponse = buildStatusResponse(analysisRun);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(processingResponse);

                case COMPLETED:
                    // 200 OK - Job completed successfully
                    AnalysisResult result = analysisService.getAnalysisResult(analysisId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Analysis marked as completed but result not found. This is a data inconsistency."));

                    Map<String, Object> completedResponse = new HashMap<>();
                    completedResponse.put("analysisId", analysisId);
                    completedResponse.put("ticker", result.getTicker());
                    completedResponse.put("status", "COMPLETED");
                    completedResponse.put("period", result.getPeriod());
                    completedResponse.put("generatedAt", result.getGeneratedAt());
                    completedResponse.put("analytics", result.getAnalytics());
                    completedResponse.put("charts", result.getCharts());

                    return ResponseEntity.ok(completedResponse);

                case FAILED:
                    // 500 INTERNAL SERVER ERROR - Job failed
                    AnalysisStatusResponse failedResponse = buildStatusResponse(analysisRun);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failedResponse);

                default:
                    // Should never happen
                    log.error("Unknown analysis status: {}", analysisRun.getStatus());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Unknown job status"));
            }

        } catch (IllegalArgumentException e) {
            // Invalid UUID format or job not found
            log.warn("Invalid analysisId or job not found: {}", analysisId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching analysis: {}", analysisId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Get latest completed analysis for a ticker
     * GET /api/analysis/ticker/{ticker}/latest
     *
     * Returns:
     * - 200 OK with latest completed analysis
     * - 404 NOT FOUND if no completed analysis exists
     */
    @GetMapping("/ticker/{ticker}/latest")
    public ResponseEntity<?> getLatestAnalysis(@PathVariable String ticker) {
        log.info("Fetching latest completed analysis for ticker={}", ticker);

        try {
            AnalysisRun analysisRun = analysisService.getLatestCompletedAnalysis(ticker)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No completed analysis found for ticker: " + ticker));

            AnalysisResult result = analysisService.getAnalysisResult(analysisRun.getId().toString())
                    .orElseThrow(() -> new RuntimeException(
                            "Analysis marked as completed but result not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("analysisId", analysisRun.getId().toString());
            response.put("ticker", result.getTicker());
            response.put("status", "COMPLETED");
            response.put("period", result.getPeriod());
            response.put("generatedAt", result.getGeneratedAt());
            response.put("analytics", result.getAnalytics());
            response.put("charts", result.getCharts());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("No completed analysis for ticker={}", ticker);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching latest analysis for ticker={}", ticker, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Helper method to build consistent status response
     */
    private AnalysisStatusResponse buildStatusResponse(AnalysisRun analysisRun) {
        String message;
        switch (analysisRun.getStatus()) {
            case PROCESSING:
                message = "Analysis in progress (" + analysisRun.getProgress() + "% complete)";
                break;
            case COMPLETED:
                message = "Analysis completed successfully";
                break;
            case FAILED:
                message = "Analysis failed: " + analysisRun.getErrorMessage();
                break;
            default:
                message = "Unknown status";
        }

        return AnalysisStatusResponse.builder()
                .analysisId(analysisRun.getId().toString())
                .ticker(analysisRun.getTicker())
                .period(analysisRun.getPeriod())
                .status(analysisRun.getStatus().name())
                .progress(analysisRun.getProgress())
                .message(message)
                .createdAt(analysisRun.getCreatedAt())
                .updatedAt(analysisRun.getUpdatedAt())
                .completedAt(analysisRun.getCompletedAt())
                .errorMessage(analysisRun.getErrorMessage())
                .build();
    }
}
