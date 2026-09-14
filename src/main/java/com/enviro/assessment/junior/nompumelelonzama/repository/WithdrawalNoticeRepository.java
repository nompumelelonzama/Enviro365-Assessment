package com.enviro.assessment.junior.nompumelelonzama.repository;

import com.enviro.assessment.junior.nompumelelonzama.entity.WithdrawalNotice;
import com.enviro.assessment.junior.nompumelelonzama.entity.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WithdrawalNoticeRepository extends JpaRepository<WithdrawalNotice, Long> {
    List<WithdrawalNotice> findByPortfolioId(Long portfolioId);
    List<WithdrawalNotice> findByPortfolioIdAndRequestDateBetween(
            Long portfolioId, LocalDateTime start, LocalDateTime end
    );
    List<WithdrawalNotice> findByPortfolioIdAndStatus(Long portfolioId, WithdrawalStatus status);
}