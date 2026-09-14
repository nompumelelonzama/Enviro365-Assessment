package com.enviro.assessment.junior.nompumelelonzama.service;

import com.enviro.assessment.junior.nompumelelonzama.entity.*;
import com.enviro.assessment.junior.nompumelelonzama.exception.*;
import com.enviro.assessment.junior.nompumelelonzama.repository.PortfolioRepository;
import com.enviro.assessment.junior.nompumelelonzama.repository.ProductRepository;
import com.enviro.assessment.junior.nompumelelonzama.repository.WithdrawalNoticeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WithdrawalService {

    private final PortfolioRepository portfolioRepository;
    private final ProductRepository productRepository;
    private final WithdrawalNoticeRepository withdrawalNoticeRepository;

    public WithdrawalService(PortfolioRepository portfolioRepository,
                             ProductRepository productRepository,
                             WithdrawalNoticeRepository withdrawalNoticeRepository) {
        this.portfolioRepository = portfolioRepository;
        this.productRepository = productRepository;
        this.withdrawalNoticeRepository = withdrawalNoticeRepository;
    }

    public WithdrawalNotice createWithdrawal(Long portfolioId, Long productId, BigDecimal amount) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Portfolio not found with id: " + portfolioId));

        Investor investor = portfolio.getInvestor();
        BigDecimal balance = portfolio.getBalance();

        Product product = portfolio.getProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId + " on portfolio " + portfolioId));

        // Rule 1: Withdrawal must not exceed balance
        if (amount.compareTo(balance) > 0) {
            String reason = "Withdrawal amount exceeds available balance.";
            saveRejectedNotice(portfolio, amount, reason);
            throw new InsufficientBalanceException(reason);
        }

        // Rule 2: Withdrawal must not exceed 90% of balance
        BigDecimal maxAllowed = balance.multiply(BigDecimal.valueOf(0.90));
        if (amount.compareTo(maxAllowed) > 0) {
            String reason = "Withdrawal amount exceeds 90% of the portfolio balance.";
            saveRejectedNotice(portfolio, amount, reason);
            throw new WithdrawalLimitExceededException(reason);
        }

        // Rule 3: Retirement withdrawals only allowed if age > 65
        boolean isRetirementProduct = product.getType() == ProductType.RETIREMENT_ANNUITY;

        if (isRetirementProduct && investor.getAge() <= 65) {
            String reason = "Retirement withdrawals are only allowed for investors over the age of 65.";
            saveRejectedNotice(portfolio, amount, reason);
            throw new AgeRestrictionException(reason);
        }

        // All rules passed — update portfolio total AND the specific product's amount
        BigDecimal newBalance = balance.subtract(amount);
        portfolio.setBalance(newBalance);
        portfolioRepository.save(portfolio);

        BigDecimal newProductAmount = product.getAmount().subtract(amount);
        product.setAmount(newProductAmount);
        productRepository.save(product);

        WithdrawalNotice notice = new WithdrawalNotice();
        notice.setAmount(amount);
        notice.setBalanceAfterWithdrawal(newBalance);
        notice.setRequestDate(LocalDateTime.now());
        notice.setStatus(WithdrawalStatus.APPROVED);
        notice.setPortfolio(portfolio);

        return withdrawalNoticeRepository.save(notice);
    }

    private void saveRejectedNotice(Portfolio portfolio, BigDecimal amount, String reason) {
        WithdrawalNotice notice = new WithdrawalNotice();
        notice.setAmount(amount);
        notice.setBalanceAfterWithdrawal(portfolio.getBalance());
        notice.setRequestDate(LocalDateTime.now());
        notice.setStatus(WithdrawalStatus.REJECTED);
        notice.setReason(reason);
        notice.setPortfolio(portfolio);
        withdrawalNoticeRepository.save(notice);
    }
}