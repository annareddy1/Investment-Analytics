package com.marketlens.dto;

import com.marketlens.model.Period;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ Analysis Request DTO with Production-Grade Validation
 *
 * Validation Rules:
 * - ticker: Required, 1-10 chars, uppercase letters/dot/dash only
 * - period: Required, must be one of: 1M, 3M, 6M, 1Y, 5Y
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisRequest {

    /**
     * Stock ticker symbol (e.g., AAPL, MSFT, BRK.B)
     * - Required (not blank)
     * - 1-10 characters
     * - Uppercase letters, dots, and dashes only
     */
    @NotBlank(message = "Ticker is required")
    @Size(min = 1, max = 10, message = "Ticker must be between 1 and 10 characters")
    @Pattern(
        regexp = "^[A-Z][A-Z0-9.-]*$",
        message = "Ticker must be uppercase letters, dots, or dashes (e.g., AAPL, BRK.B)"
    )
    private String ticker;

    /**
     * Analysis time period
     * - Required (not null)
     * - Must be one of: 1M, 3M, 6M, 1Y, 5Y
     * - Case-insensitive (will be normalized)
     */
    @NotBlank(message = "Period is required")
    @Pattern(
        regexp = "^(1M|3M|6M|1Y|5Y)$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Period must be one of: 1M, 3M, 6M, 1Y, 5Y"
    )
    private String period = "1Y";

    /**
     * Get period as enum (for internal use)
     */
    public Period getPeriodEnum() {
        return Period.fromString(period);
    }
}
