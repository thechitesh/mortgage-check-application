package com.myorg.mortgage.service;

import com.myorg.mortgage.app.model.InterestRateDto;
import com.myorg.mortgage.app.model.MortgageRequestDto;
import com.myorg.mortgage.app.model.MortgageResponseDto;
import com.myorg.mortgage.model.InterestRate;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.math.BigDecimal.valueOf;

@SpringBootTest
class MortgageServiceTest {

    @Autowired
    private MortgageService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "maxMultiplier", 4);
    }

    private static Stream<Arguments> provideInterestRates() {
        return Stream.of(
                Arguments.of(19, valueOf(2)),
                Arguments.of(16, valueOf(1.5)),
                Arguments.of(25, valueOf(2.5)));
    }

    private static Stream<Arguments> provideMortgageCalculationResponse() {
        return Stream.of(
                Arguments.of(buildMortgageRequest(valueOf(200), valueOf(200), valueOf(40), 10),
                        buildResponse(false, null)),
                Arguments.of(buildMortgageRequest(valueOf(100), valueOf(200), valueOf(70), 10),
                        buildResponse(false, null)),
                Arguments.of(buildMortgageRequest(valueOf(200), valueOf(200), valueOf(60), 10),
                        buildResponse(true, BigDecimal.valueOf(1.91))),
                Arguments.of(buildMortgageRequest(valueOf(20000), valueOf(20000), valueOf(6000), 10),
                        buildResponse(true, BigDecimal.valueOf(190.91))));
    }

    @Test
    void test_GetInterestRates() {
        List<InterestRateDto> interestRates = service.getInterestRates();

        val dto1 = createDto(10, valueOf(2.8));
        val dto2 = createDto(15, valueOf(3));
        val dto3 = createDto(20, valueOf(3.25));
        val dto4 = createDto(25, valueOf(3.5));
        val dto5 = createDto(30, valueOf(3.75));
        val dto6 = createDto(40, valueOf(4));

        List<InterestRateDto> rateDtos = List.of(dto1, dto2, dto3, dto4, dto5, dto6);
        Assertions.assertThat(interestRates).hasSize(6)
                .usingRecursiveComparison()
                .ignoringFields("lastUpdate")
                .isEqualTo(rateDtos);
    }

    @ParameterizedTest
    @MethodSource("provideInterestRates")
    void test_SearchNearestInterestRate(int maturityPeriod, BigDecimal expectedRate) {
        List<InterestRate> interestRates = getInterestRates();
        BigDecimal rate = service.searchNearestInterestRate(interestRates, maturityPeriod);
        Assertions.assertThat(rate).isEqualTo(expectedRate);
    }

    @ParameterizedTest
    @MethodSource("provideMortgageCalculationResponse")
    void test_monthlyCost(MortgageRequestDto request, MortgageResponseDto response) {
        MortgageResponseDto mortgageResponseDto = service.calculateMortgage(request);
        Assertions.assertThat(mortgageResponseDto).isEqualTo(response);
    }

    @Test
    void test_checkFeasibility_4timeIncome() {
        val request = buildMortgageRequest(valueOf(200), valueOf(200), valueOf(40), 10);
        MortgageResponseDto mortgageResponseDto = service.calculateMortgage(request);
        Assertions.assertThat(mortgageResponseDto.isFeasible()).isFalse();
    }

    @Test
    void test_checkFeasibility_GreaterThanHomeValue() {
        val request = buildMortgageRequest(valueOf(100), valueOf(200), valueOf(60), 10);
        MortgageResponseDto mortgageResponseDto = service.calculateMortgage(request);
        Assertions.assertThat(mortgageResponseDto.isFeasible()).isFalse();
    }

    @Test
    void test_checkFeasibility_CalculateMonthly() {
        val request = buildMortgageRequest(valueOf(200), valueOf(200), valueOf(60), 10);
        MortgageResponseDto mortgageResponseDto = service.calculateMortgage(request);
        Assertions.assertThat(mortgageResponseDto.isFeasible()).isTrue();

    }

    private static MortgageRequestDto buildMortgageRequest(BigDecimal homeValue, BigDecimal loanValue,
            BigDecimal income, int maturity) {
        return MortgageRequestDto.builder()
                .homeValue(homeValue)
                .loanValue(loanValue)
                .income(income)
                .maturityPeriod(maturity)
                .build();
    }

    private static MortgageResponseDto buildResponse(boolean feasible, BigDecimal monthlyCost) {
        BigDecimal monthly = null;
        if (monthlyCost != null) {
            monthly = monthlyCost;
        }

        return MortgageResponseDto.builder().feasible(feasible).monthlyCost(monthly).build();
    }

    private List<InterestRate> getInterestRates() {
        val interest1 = create(20, valueOf(2));
        val interest2 = create(15, valueOf(1.5));
        val interest3 = create(25, valueOf(2.5));
        val interest4 = create(10, valueOf(1));
        return List.of(interest1, interest2, interest3, interest4);
    }

    private InterestRate create(int maturity, BigDecimal rate) {
        return new InterestRate(maturity, rate, OffsetDateTime.now());
    }

    private InterestRateDto createDto(int maturity, BigDecimal rate) {
        return InterestRateDto.builder()
                .interestRate(rate)
                .maturityPeriod(maturity)
                .build();
    }

}
