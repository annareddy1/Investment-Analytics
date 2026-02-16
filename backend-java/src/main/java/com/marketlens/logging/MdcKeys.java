package com.marketlens.logging;

/**
 * ✅ MDC (Mapped Diagnostic Context) Keys
 * - Constants for structured logging correlation
 * - Used to track requests and analysis jobs across log lines
 */
public final class MdcKeys {
    public static final String REQUEST_ID = "requestId";
    public static final String ANALYSIS_ID = "analysisId";
    public static final String TICKER = "ticker";
    public static final String PERIOD = "period";

    private MdcKeys() {
        // Utility class - prevent instantiation
    }
}
