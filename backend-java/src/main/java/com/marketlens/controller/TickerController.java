package com.marketlens.controller;

import com.marketlens.dto.TickerInfo;
import com.marketlens.service.TickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickers")
@RequiredArgsConstructor
public class TickerController {

    private final TickerService tickerService;

    @GetMapping("/presets")
    public ResponseEntity<Map<String, List<TickerInfo>>> getPresetTickers() {
        return ResponseEntity.ok(
                Map.of("tickers", tickerService.getPresetTickers())
        );
    }
}