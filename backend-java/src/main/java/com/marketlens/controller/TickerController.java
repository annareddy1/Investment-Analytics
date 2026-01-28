package com.marketlens.controller;

import com.marketlens.dto.TickerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class TickerController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("message", "MarketLens API is running"));
    }
    
    @GetMapping("/tickers/presets")
    public ResponseEntity<Map<String, List<TickerInfo>>> getPresetTickers() {
        log.info("Fetching preset tickers");
        
        List<TickerInfo> tickers = Arrays.asList(
                new TickerInfo("AAPL", "Apple Inc."),
                new TickerInfo("MSFT", "Microsoft Corp."),
                new TickerInfo("TSLA", "Tesla Inc."),
                new TickerInfo("SPY", "S&P 500 ETF"),
                new TickerInfo("NVDA", "NVIDIA Corp.")
        );
        
        Map<String, List<TickerInfo>> response = new HashMap<>();
        response.put("tickers", tickers);
        
        return ResponseEntity.ok(response);
    }
}