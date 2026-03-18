package com.marketlens.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Period {
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
    FIVE_YEARS("5Y");

    private final String value;

    Period(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Period fromString(String value) {
        for (Period period : Period.values()) {
            if (period.value.equalsIgnoreCase(value)) {
                return period;
            }
        }

        throw new IllegalArgumentException(
                "Invalid period: " + value + ". Allowed values: 1M, 3M, 6M, 1Y, 5Y"
        );
    }

    @Override
    public String toString() {
        return value;
    }
}