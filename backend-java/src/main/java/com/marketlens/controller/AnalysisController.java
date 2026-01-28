package com.marketlens.controller;

import com.marketlens.dto.AnalysisRequest;
import com.marketlens.dto.AnalysisResponse;
import com.marketlens.model.AnalysisResult;
import com.marketlens.model.AnalysisRun;
import com.marketlens.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {
    
    private final AnalysisService analysisService;
    
    @PostMapping("/run")
    public ResponseEntity<AnalysisResponse> runAnalysis(@RequestBody AnalysisRequest request) {
        log.info("Received analysis request for ticker: {}", request.getTicker());
        
        try {
            // Create analysis run
            AnalysisRun analysisRun = analysisService.createAnalysisRun(
                    request.getTicker(),
                    request.getPeriod()
            );
            
            // Start async processing
            analysisService.processAnalysis(analysisRun.getId());
            
            // Return immediate response
            AnalysisResponse response = AnalysisResponse.builder()
                    .analysisId(analysisRun.getId().toString())
                    .ticker(analysisRun.getTicker())
                    .status(analysisRun.getStatus().name())
                    .message("Analysis job started")
                    .progress(0)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AnalysisResponse.builder()
                            .message("Failed to start analysis: " + e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getAnalysis(@PathVariable String analysisId) {
        log.info("Fetching analysis: {}", analysisId);
        
        try {
            UUID uuid = UUID.fromString(analysisId);
            AnalysisRun analysisRun = analysisService.getAnalysisRun(uuid)
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));
            
            if (analysisRun.getStatus() == AnalysisRun.AnalysisStatus.COMPLETED) {
                // Return full result
                AnalysisResult result = analysisService.getAnalysisResult(analysisId)
                        .orElseThrow(() -> new RuntimeException("Analysis result not found"));
                
                Map<String, Object> response = new HashMap<>();
                response.put("analysisId", analysisId);
                response.put("ticker", result.getTicker());
                response.put("status", "COMPLETED");
                response.put("generatedAt", result.getGeneratedAt());
                response.put("period", result.getPeriod());
                response.put("analytics", result.getAnalytics());
                response.put("charts", result.getCharts());
                
                return ResponseEntity.ok(response);
                
            } else if (analysisRun.getStatus() == AnalysisRun.AnalysisStatus.PROCESSING) {
                // Return status
                AnalysisResponse response = AnalysisResponse.builder()
                        .analysisId(analysisId)
                        .ticker(analysisRun.getTicker())
                        .status("PROCESSING")
                        .progress(analysisRun.getProgress())
                        .message("Analysis in progress...")
                        .build();
                
                return ResponseEntity.ok(response);
                
            } else {
                // Failed
                AnalysisResponse response = AnalysisResponse.builder()
                        .analysisId(analysisId)
                        .ticker(analysisRun.getTicker())
                        .status("FAILED")
                        .message(analysisRun.getErrorMessage())
                        .build();
                
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("Error fetching analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/ticker/{ticker}/latest")
    public ResponseEntity<?> getLatestAnalysis(@PathVariable String ticker) {
        log.info("Fetching latest analysis for ticker: {}", ticker);
        
        try {
            AnalysisRun analysisRun = analysisService.getLatestCompletedAnalysis(ticker)
                    .orElseThrow(() -> new RuntimeException("No completed analysis found for ticker"));
            
            AnalysisResult result = analysisService.getAnalysisResult(analysisRun.getId().toString())
                    .orElseThrow(() -> new RuntimeException("Analysis result not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysisId", analysisRun.getId().toString());
            response.put("ticker", result.getTicker());
            response.put("status", "COMPLETED");
            response.put("generatedAt", result.getGeneratedAt());
            response.put("period", result.getPeriod());
            response.put("analytics", result.getAnalytics());
            response.put("charts", result.getCharts());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching latest analysis", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}