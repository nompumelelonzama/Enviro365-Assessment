package com.enviro.assessment.junior.nompumelelonzama.controller;

import com.enviro.assessment.junior.nompumelelonzama.entity.Portfolio;
import com.enviro.assessment.junior.nompumelelonzama.service.PortfolioService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/investor/{investorId}")
    public Portfolio getPortfolioByInvestor(
            @PathVariable @Positive(message = "investorId must be a positive number") Long investorId) {
        return portfolioService.getPortfolioByInvestorId(investorId);
    }
}