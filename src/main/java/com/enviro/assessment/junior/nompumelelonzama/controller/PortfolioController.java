package com.enviro.assessment.junior.nompumelelonzama.controller;

import com.enviro.assessment.junior.nompumelelonzama.entity.Portfolio;
import com.enviro.assessment.junior.nompumelelonzama.service.PortfolioService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/investor/{investorId}")
    public Portfolio getPortfolioByInvestor(@PathVariable Long investorId) {
        return portfolioService.getPortfolioByInvestorId(investorId);
    }
}