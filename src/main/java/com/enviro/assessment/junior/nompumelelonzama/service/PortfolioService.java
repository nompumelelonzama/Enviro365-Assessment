package com.enviro.assessment.junior.nompumelelonzama.service;

import com.enviro.assessment.junior.nompumelelonzama.entity.Portfolio;
import com.enviro.assessment.junior.nompumelelonzama.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.nompumelelonzama.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public Portfolio getPortfolioByInvestorId(Long investorId) {
        return portfolioRepository.findByInvestorId(investorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Portfolio not found for investor id: " + investorId));
    }

    public Portfolio getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Portfolio not found with id: " + portfolioId));
    }
}