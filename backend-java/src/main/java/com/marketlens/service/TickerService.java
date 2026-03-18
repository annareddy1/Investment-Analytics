package com.marketlens.service;

import com.marketlens.dto.TickerInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TickerService {

    public List<TickerInfo> getPresetTickers() {
        return List.of(
                new TickerInfo("AAPL", "Apple Inc."),
                new TickerInfo("MSFT", "Microsoft Corp."),
                new TickerInfo("TSLA", "Tesla Inc."),
                new TickerInfo("SPY", "S&P 500 ETF"),
                new TickerInfo("NVDA", "NVIDIA Corp.")
        );
    }
}