package com.enviro.assessment.junior.nompumelelonzama.service;

import com.enviro.assessment.junior.nompumelelonzama.entity.WithdrawalNotice;
import com.enviro.assessment.junior.nompumelelonzama.entity.WithdrawalStatus;
import com.enviro.assessment.junior.nompumelelonzama.repository.WithdrawalNoticeRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WithdrawalHistoryService {

    private final WithdrawalNoticeRepository withdrawalNoticeRepository;

    public WithdrawalHistoryService(WithdrawalNoticeRepository withdrawalNoticeRepository) {
        this.withdrawalNoticeRepository = withdrawalNoticeRepository;
    }

    public List<WithdrawalNotice> getHistory(Long portfolioId, LocalDateTime start, LocalDateTime end, WithdrawalStatus status) {
        List<WithdrawalNotice> notices;

        if (start != null && end != null) {
            notices = withdrawalNoticeRepository.findByPortfolioIdAndRequestDateBetween(portfolioId, start, end);
        } else {
            notices = withdrawalNoticeRepository.findByPortfolioId(portfolioId);
        }

        if (status != null) {
            notices = notices.stream()
                    .filter(notice -> notice.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return notices;
    }

    public byte[] generateCsv(Long portfolioId, LocalDateTime start, LocalDateTime end, WithdrawalStatus status) {
        List<WithdrawalNotice> notices = getHistory(portfolioId, start, end, status);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        writer.println("ID,Amount,Balance After Withdrawal,Request Date,Status");

        for (WithdrawalNotice notice : notices) {
            writer.printf("%d,%s,%s,%s,%s%n",
                    notice.getId(),
                    notice.getAmount(),
                    notice.getBalanceAfterWithdrawal(),
                    notice.getRequestDate().format(formatter),
                    notice.getStatus()
            );
        }

        writer.flush();
        return outputStream.toByteArray();
    }
}