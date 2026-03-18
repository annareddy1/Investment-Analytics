package com.marketlens.service;

import com.marketlens.model.AnalysisResult;
import com.marketlens.model.AnalysisRun;
import com.marketlens.repository.AnalysisResultRepository;
import com.marketlens.repository.AnalysisRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private static final int PROGRESS_FETCHING_DATA = 20;
    private static final int PROGRESS_CALCULATING_ANALYTICS = 50;
    private static final int PROGRESS_GENERATING_CHARTS = 70;
    private static final int PROGRESS_SAVING_RESULTS = 90;
    private static final int PROGRESS_COMPLETED = 100;

    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final YahooFinanceService yahooFinanceService;
    private final AnalyticsCalculationService analyticsCalculationService;

    public AnalysisRun createAnalysisRun(String ticker, String period) {
        AnalysisRun run = new AnalysisRun();
        run.setTicker(ticker.toUpperCase());
        run.setPeriod(period.toUpperCase());
        run.setStatus(AnalysisRun.AnalysisStatus.PROCESSING);
        run.setProgress(0);

        AnalysisRun savedRun = analysisRunRepository.save(run);
        log.info("Created analysis run {} for ticker {}", savedRun.getId(), savedRun.getTicker());

        return savedRun;
    }

    @Async("analysisExecutor")
    public void processAnalysis(UUID analysisId) {
        log.info("Started analysis processing for {}", analysisId);

        AnalysisRun run = findAnalysisRunOrThrow(analysisId);

        try {
            List<YahooFinanceService.StockPrice> prices = fetchMarketData(run);
            AnalysisResult.Analytics analytics = calculateAnalytics(run, prices);
            AnalysisResult.Charts charts = generateCharts(run, prices);

            saveResult(run, analytics, charts);
            markCompleted(run);

            log.info("Completed analysis processing for {}", analysisId);

        } catch (Exception e) {
            log.error("Analysis failed for {}", analysisId, e);
            markFailed(run, e.getMessage());
        }
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

    private AnalysisRun findAnalysisRunOrThrow(UUID analysisId) {
        return analysisRunRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis run not found: " + analysisId));
    }

    private List<YahooFinanceService.StockPrice> fetchMarketData(AnalysisRun run) {
        updateProgress(run, PROGRESS_FETCHING_DATA);
        return yahooFinanceService.fetchHistoricalData(run.getTicker(), run.getPeriod());
    }

    private AnalysisResult.Analytics calculateAnalytics(
            AnalysisRun run,
            List<YahooFinanceService.StockPrice> prices
    ) {
        updateProgress(run, PROGRESS_CALCULATING_ANALYTICS);
        return analyticsCalculationService.calculateAnalytics(prices);
    }

    private AnalysisResult.Charts generateCharts(
            AnalysisRun run,
            List<YahooFinanceService.StockPrice> prices
    ) {
        updateProgress(run, PROGRESS_GENERATING_CHARTS);
        return analyticsCalculationService.generateCharts(prices);
    }

    private void saveResult(
            AnalysisRun run,
            AnalysisResult.Analytics analytics,
            AnalysisResult.Charts charts
    ) {
        updateProgress(run, PROGRESS_SAVING_RESULTS);

        AnalysisResult result = new AnalysisResult();
        result.setAnalysisId(run.getId().toString());
        result.setTicker(run.getTicker());
        result.setPeriod(run.getPeriod());
        result.setGeneratedAt(LocalDateTime.now());
        result.setAnalytics(analytics);
        result.setCharts(charts);

        analysisResultRepository.save(result);
    }

    private void markCompleted(AnalysisRun run) {
        run.setStatus(AnalysisRun.AnalysisStatus.COMPLETED);
        run.setProgress(PROGRESS_COMPLETED);
        run.setCompletedAt(LocalDateTime.now());
        analysisRunRepository.save(run);
    }

    private void markFailed(AnalysisRun run, String errorMessage) {
        run.setStatus(AnalysisRun.AnalysisStatus.FAILED);
        run.setErrorMessage(errorMessage);
        run.setCompletedAt(LocalDateTime.now());
        analysisRunRepository.save(run);
    }

    private void updateProgress(AnalysisRun run, int progress) {
        run.setProgress(progress);
        analysisRunRepository.save(run);
        log.info("Analysis {} progress updated to {}%", run.getId(), progress);
    }
}