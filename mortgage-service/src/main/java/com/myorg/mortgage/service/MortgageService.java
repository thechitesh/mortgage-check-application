package com.myorg.mortgage.service;

import com.myorg.mortgage.app.model.InterestRateDto;
import com.myorg.mortgage.app.model.MortgageRequestDto;
import com.myorg.mortgage.app.model.MortgageResponseDto;
import com.myorg.mortgage.exception.MortgageException;
import com.myorg.mortgage.model.InterestRate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MortgageService {

    private final List<InterestRate> interestRates = new ArrayList<>();

    @Value("${mortgage.max-multiplier}")
    private Integer maxMultiplier;

    @PostConstruct
    public void init() {
        interestRates.add(new InterestRate(10, new BigDecimal("2.8"), OffsetDateTime.now()));
        interestRates.add(new InterestRate(15, new BigDecimal("3"), OffsetDateTime.now()));
        interestRates.add(new InterestRate(20, new BigDecimal("3.25"), OffsetDateTime.now()));
        interestRates.add(new InterestRate(25, new BigDecimal("3.5"), OffsetDateTime.now()));
        interestRates.add(new InterestRate(30, new BigDecimal("3.75"), OffsetDateTime.now()));
        interestRates.add(new InterestRate(40, new BigDecimal("4"), OffsetDateTime.now()));
    }

    public List<InterestRateDto> getInterestRates() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
        return interestRates.stream()
                .map(rate -> mapper.map(rate, InterestRateDto.class))
                .toList();
    }

    public MortgageResponseDto calculateMortgage(MortgageRequestDto requestDto) {
        boolean feasible = validateIncome(requestDto.getIncome(), requestDto.getLoanValue())
                && validateMortgageAmount(requestDto.getHomeValue(), requestDto.getLoanValue());
        if (feasible) {
            BigDecimal monthlyAmount = calculateMonthlyAmount(requestDto);
            return MortgageResponseDto.builder().feasible(true).monthlyCost(monthlyAmount).build();
        }
        return MortgageResponseDto.builder().feasible(false).build();
    }

    private boolean validateMortgageAmount(BigDecimal homeValue, BigDecimal loanValue) {
        return loanValue.compareTo(homeValue) <= 0;
    }

    private boolean validateIncome(BigDecimal income, BigDecimal loanValue) {
        return loanValue.compareTo(income.multiply(BigDecimal.valueOf(maxMultiplier))) <= 0;
    }

    private BigDecimal calculateMonthlyAmount(MortgageRequestDto requestDto) {
        BigDecimal loanValue = requestDto.getLoanValue();
        int maturityInMonths = requestDto.getMaturityPeriod() * 12;
        BigDecimal annualInterestRate = searchNearestInterestRate(interestRates, requestDto.getMaturityPeriod());
        log.debug("annual interest rate {}", annualInterestRate);
        BigDecimal monthlyPayment = monthlyAmount(annualInterestRate, loanValue, maturityInMonths);
        log.info("Monthly payment {}", monthlyPayment);
        return monthlyPayment;
    }

    private static BigDecimal monthlyAmount(BigDecimal annualInterestRate, BigDecimal loanValue, int months) {
        BigDecimal monthlyInterest = annualInterestRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        BigDecimal numerator = loanValue.multiply(monthlyInterest)
                .multiply(monthlyInterest.add(BigDecimal.ONE).pow(months));
        BigDecimal denominator = (BigDecimal.ONE.add(monthlyInterest).pow(months)).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal searchNearestInterestRate(List<InterestRate> rates, int maturityPeriod) {
        return rates.stream()
                .min(Comparator.comparingInt(r -> Math.abs(r.maturityPeriod() - maturityPeriod)))
                .map(InterestRate::interestRate)
                .orElseThrow(() -> new MortgageException("No interest rates available"));
    }

}
