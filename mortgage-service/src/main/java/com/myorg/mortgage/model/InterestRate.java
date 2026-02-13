package com.myorg.mortgage.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InterestRate(
        Integer maturityPeriod,
        BigDecimal interestRate,
        OffsetDateTime lastUpdate) {
}
