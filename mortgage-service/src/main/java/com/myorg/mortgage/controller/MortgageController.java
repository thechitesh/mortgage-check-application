package com.myorg.mortgage.controller;

import com.myorg.mortgage.app.api.ApiApi;
import com.myorg.mortgage.app.model.InterestRateDto;
import com.myorg.mortgage.app.model.InterestRateResponseDto;
import com.myorg.mortgage.app.model.MortgageRequestDto;
import com.myorg.mortgage.app.model.MortgageResponseDto;
import com.myorg.mortgage.service.MortgageService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MortgageController implements ApiApi {

    private final MortgageService mortgageService;

    @Override
    @RateLimiter(name = "mortgageService", fallbackMethod = "getMortgageInterestRateFallback")
    public ResponseEntity<InterestRateResponseDto> getMortgageInterestRate() {
        List<InterestRateDto> rateDTOs = mortgageService.getInterestRates();
        return ResponseEntity.ok(InterestRateResponseDto.builder().interestRates(rateDTOs).build());
    }



    @Override
    @RateLimiter(name = "mortgageService", fallbackMethod = "checkMortgageFallback")
    public ResponseEntity<MortgageResponseDto> checkMortgage(MortgageRequestDto mortgageRequestDto) {
        return ResponseEntity.ok(mortgageService.calculateMortgage(mortgageRequestDto));
    }

    public ResponseEntity<InterestRateResponseDto> getMortgageInterestRateFallback(
            io.github.resilience4j.ratelimiter.RequestNotPermitted ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS).build();
    }

    public ResponseEntity<MortgageResponseDto> checkMortgageFallback(MortgageRequestDto mortgageRequestDto,
            io.github.resilience4j.ratelimiter.RequestNotPermitted ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS).build();
    }

}
