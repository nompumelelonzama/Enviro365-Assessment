package com.enviro.assessment.junior.nompumelelonzama.service;

import com.enviro.assessment.junior.nompumelelonzama.entity.*;
import com.enviro.assessment.junior.nompumelelonzama.exception.*;
import com.enviro.assessment.junior.nompumelelonzama.repository.PortfolioRepository;
import com.enviro.assessment.junior.nompumelelonzama.repository.ProductRepository;
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
    private ProductRepository productRepository;

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

    private Product buildProduct(Long id, ProductType type, BigDecimal amount) {
        Product product = new Product();
        product.setId(id);
        product.setName(type.name());
        product.setType(type);
        product.setAmount(amount);
        product.setInitialAmount(amount);
        product.setPortfolio(portfolio);
        return product;
    }

    @Test
    void createWithdrawal_succeedsForValidAmountWithNoRetirementProduct() {
        Product unitTrust = buildProduct(1L, ProductType.UNIT_TRUST, new BigDecimal("100000.00"));
        portfolio.setProducts(List.of(unitTrust));

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalNotice notice = withdrawalService.createWithdrawal(1L, 1L, new BigDecimal("50000.00"));

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
                () -> withdrawalService.createWithdrawal(99L, 1L, new BigDecimal("1000.00")));

        verify(withdrawalNoticeRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsResourceNotFoundException_whenProductDoesNotExist() {
        portfolio.setProducts(new ArrayList<>());
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        assertThrows(ResourceNotFoundException.class,
                () -> withdrawalService.createWithdrawal(1L, 99L, new BigDecimal("1000.00")));

        verify(withdrawalNoticeRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsInsufficientBalanceException_whenAmountExceedsBalance() {
        Product unitTrust = buildProduct(1L, ProductType.UNIT_TRUST, new BigDecimal("100000.00"));
        portfolio.setProducts(List.of(unitTrust));
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(InsufficientBalanceException.class,
                () -> withdrawalService.createWithdrawal(1L, 1L, new BigDecimal("150000.00")));

        verify(portfolioRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsWithdrawalLimitExceededException_whenAmountExceeds90PercentOfBalance() {
        Product unitTrust = buildProduct(1L, ProductType.UNIT_TRUST, new BigDecimal("100000.00"));
        portfolio.setProducts(List.of(unitTrust));
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(WithdrawalLimitExceededException.class,
                () -> withdrawalService.createWithdrawal(1L, 1L, new BigDecimal("95000.00")));

        verify(portfolioRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_throwsAgeRestrictionException_whenRetirementProductAndAgeUnder65() {
        investor.setAge(40);
        Product retirementAnnuity = buildProduct(2L, ProductType.RETIREMENT_ANNUITY, new BigDecimal("100000.00"));
        portfolio.setProducts(List.of(retirementAnnuity));
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(AgeRestrictionException.class,
                () -> withdrawalService.createWithdrawal(1L, 2L, new BigDecimal("10000.00")));

        verify(portfolioRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_succeeds_whenRetirementProductAndAgeOver65() {
        investor.setAge(70);
        Product retirementAnnuity = buildProduct(2L, ProductType.RETIREMENT_ANNUITY, new BigDecimal("100000.00"));
        portfolio.setProducts(List.of(retirementAnnuity));
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalNotice notice = withdrawalService.createWithdrawal(1L, 2L, new BigDecimal("10000.00"));

        assertEquals(WithdrawalStatus.APPROVED, notice.getStatus());
        assertEquals(new BigDecimal("90000.00"), portfolio.getBalance());
    }
}