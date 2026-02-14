package com.myorg.mortgage.controller;

import com.myorg.mortgage.app.api.ApiApi;
import com.myorg.mortgage.app.model.*;
import com.myorg.mortgage.service.MortgageService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

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

    public ResponseEntity<ErrorDto> getMortgageInterestRateFallback(RequestNotPermitted ex) {
        return ResponseEntity.status(TOO_MANY_REQUESTS).body(ErrorDto.builder().message("Too many request").build());
    }

    public ResponseEntity<ErrorDto> checkMortgageFallback(MortgageRequestDto mortgageRequestDto, RequestNotPermitted ex) {
        return ResponseEntity.status(TOO_MANY_REQUESTS).body(ErrorDto.builder().message("Too many request").build());
    }

}
