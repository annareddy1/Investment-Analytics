package com.marketlens.service;

import com.marketlens.model.AnalysisResult;
import com.marketlens.model.AnalysisRun;
import com.marketlens.repository.AnalysisResultRepository;
import com.marketlens.repository.AnalysisRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {
    
    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final YahooFinanceService yahooFinanceService;
    private final AnalyticsCalculationService analyticsCalculationService;
    
    @Transactional
    public AnalysisRun createAnalysisRun(String ticker, String period) {
        AnalysisRun analysisRun = new AnalysisRun();
        analysisRun.setTicker(ticker.toUpperCase());
        analysisRun.setPeriod(period);
        analysisRun.setStatus(AnalysisRun.AnalysisStatus.PROCESSING);
        analysisRun.setProgress(0);
        
        analysisRun = analysisRunRepository.save(analysisRun);
        log.info("Created analysis run: {} for ticker: {}", analysisRun.getId(), ticker);
        
        return analysisRun;
    }
    
    @Async("analysisExecutor")
    public void processAnalysis(UUID analysisId) {
        log.info("Starting async analysis processing for: {}", analysisId);
        
        try {
            // Fetch analysis run
            AnalysisRun analysisRun = analysisRunRepository.findById(analysisId)
                    .orElseThrow(() -> new RuntimeException("Analysis run not found"));
            
            // Update progress
            updateProgress(analysisRun, 20, "Fetching market data...");
            
            // Fetch historical data from Yahoo Finance
            List<YahooFinanceService.StockPrice> prices = yahooFinanceService
                    .fetchHistoricalData(analysisRun.getTicker(), analysisRun.getPeriod());
            
            updateProgress(analysisRun, 50, "Calculating analytics...");
            
            // Calculate analytics
            AnalysisResult.Analytics analytics = analyticsCalculationService
                    .calculateAnalytics(prices);
            
            updateProgress(analysisRun, 70, "Generating charts...");
            
            // Generate charts
            AnalysisResult.Charts charts = analyticsCalculationService
                    .generateCharts(prices);
            
            updateProgress(analysisRun, 90, "Saving results...");
            
            // Create and save result
            AnalysisResult result = new AnalysisResult();
            result.setAnalysisId(analysisId.toString());
            result.setTicker(analysisRun.getTicker());
            result.setPeriod(analysisRun.getPeriod());
            result.setGeneratedAt(LocalDateTime.now());
            result.setAnalytics(analytics);
            result.setCharts(charts);
            
            analysisResultRepository.save(result);
            
            // Mark as completed
            analysisRun.setStatus(AnalysisRun.AnalysisStatus.COMPLETED);
            analysisRun.setCompletedAt(LocalDateTime.now());
            analysisRun.setProgress(100);
            analysisRunRepository.save(analysisRun);
            
            log.info("Completed analysis processing for: {}", analysisId);
            
        } catch (Exception e) {
            log.error("Error processing analysis: {}", analysisId, e);
            
            // Mark as failed
            AnalysisRun analysisRun = analysisRunRepository.findById(analysisId)
                    .orElseThrow(() -> new RuntimeException("Analysis run not found"));
            
            analysisRun.setStatus(AnalysisRun.AnalysisStatus.FAILED);
            analysisRun.setErrorMessage(e.getMessage());
            analysisRun.setCompletedAt(LocalDateTime.now());
            analysisRunRepository.save(analysisRun);
        }
    }
    
    private void updateProgress(AnalysisRun analysisRun, int progress, String message) {
        analysisRun.setProgress(progress);
        analysisRunRepository.save(analysisRun);
        log.info("Analysis {} progress: {}% - {}", analysisRun.getId(), progress, message);
    }
    
    public Optional<AnalysisRun> getAnalysisRun(UUID analysisId) {
        return analysisRunRepository.findById(analysisId);
    }
    
    public Optional<AnalysisResult> getAnalysisResult(String analysisId) {
        return analysisResultRepository.findByAnalysisId(analysisId);
    }
    
    public Optional<AnalysisRun> getLatestCompletedAnalysis(String ticker) {
        return analysisRunRepository.findTopByTickerAndStatusOrderByCreatedAtDesc(
                ticker.toUpperCase(), 
                AnalysisRun.AnalysisStatus.COMPLETED
        );
    }
}
