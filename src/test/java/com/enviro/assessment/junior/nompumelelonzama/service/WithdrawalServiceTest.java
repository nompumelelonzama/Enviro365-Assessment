package com.enviro.assessment.junior.nompumelelonzama.service;

import com.enviro.assessment.junior.nompumelelonzama.entity.*;
import com.enviro.assessment.junior.nompumelelonzama.exception.*;
import com.enviro.assessment.junior.nompumelelonzama.repository.PortfolioRepository;
import com.enviro.assessment.junior.nompumelelonzama.repository.WithdrawalNoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private WithdrawalNoticeRepository withdrawalNoticeRepository;

    @InjectMocks
    private WithdrawalService withdrawalService;

    private Portfolio portfolio;
    private Investor investor;

    @BeforeEach
    void setUp() {
        investor = new Investor();
        investor.setId(1L);
        investor.setFirstName("Test");
        investor.setLastName("Investor");
        investor.setAge(40);
        investor.setEmail("test@example.com");

        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setBalance(new BigDecimal("100000.00"));
        portfolio.setInvestor(investor);
        portfolio.setProducts(new ArrayList<>());
    }

    private Product buildProduct(ProductType type, BigDecimal amount) {
        Product product = new Product();
        product.setName(type.name());
        product.setType(type);
        product.setAmount(amount);
        product.setPortfolio(portfolio);
        return product;
    }

    @Test
    void createWithdrawal_succeedsForValidAmountWithNoRetirementProduct() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalNotice notice = withdrawalService.createWithdrawal(1L, new BigDecimal("50000.00"));

        assertEquals(WithdrawalStatus.APPROVED, notice.getStatus());
        assertEquals(new BigDecimal("50000.00"), notice.getBalanceAfterWithdrawal());
        assertEquals(new BigDecimal("50000.00"), portfolio.getBalance());
        verify(portfolioRepository).save(portfolio);
        verify(withdrawalNoticeRepository).save(any(WithdrawalNotice.class));
    }

    @Test
    void createWithdrawal_throwsResourceNotFoundException_whenPortfolioDoesNotExist() {
        when(portfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> withdrawalService.createWithdrawal(99L, new BigDecimal("1000.00")));

        verify(withdrawalNoticeRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsInsufficientBalanceException_whenAmountExceedsBalance() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        assertThrows(InsufficientBalanceException.class,
                () -> withdrawalService.createWithdrawal(1L, new BigDecimal("150000.00")));

        verify(portfolioRepository, never()).save(any());
        verify(withdrawalNoticeRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsWithdrawalLimitExceededException_whenAmountExceeds90PercentOfBalance() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        // 95% of 100,000 = 95,000 — over the 90% cap but under the full balance
        assertThrows(WithdrawalLimitExceededException.class,
                () -> withdrawalService.createWithdrawal(1L, new BigDecimal("95000.00")));

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsAgeRestrictionException_whenRetirementProductAndAgeUnder65() {
        investor.setAge(40);
        portfolio.setProducts(List.of(buildProduct(ProductType.RETIREMENT_ANNUITY, new BigDecimal("100000.00"))));
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        assertThrows(AgeRestrictionException.class,
                () -> withdrawalService.createWithdrawal(1L, new BigDecimal("10000.00")));

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_succeeds_whenRetirementProductAndAgeOver65() {
        investor.setAge(70);
        portfolio.setProducts(List.of(buildProduct(ProductType.RETIREMENT_ANNUITY, new BigDecimal("100000.00"))));
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalNotice notice = withdrawalService.createWithdrawal(1L, new BigDecimal("10000.00"));

        assertEquals(WithdrawalStatus.APPROVED, notice.getStatus());
        assertEquals(new BigDecimal("90000.00"), portfolio.getBalance());
    }
}