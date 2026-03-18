package com.marketlens.dto;

import com.marketlens.model.Period;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @NotBlank(message = "Ticker is required")
    @Size(min = 1, max = 10, message = "Ticker must be between 1 and 10 characters")
    @Pattern(
            regexp = "^[A-Z][A-Z0-9.-]*$",
            message = "Ticker must contain uppercase letters, numbers, dots, or dashes"
    )
    private String ticker;

    @NotBlank(message = "Period is required")
    @Pattern(
            regexp = "^(1M|3M|6M|1Y|5Y)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Period must be one of: 1M, 3M, 6M, 1Y, 5Y"
    )
    private String period = "1Y";

    public Period getPeriodEnum() {
        return Period.fromString(period);
    }
}