package com.enviro.assessment.junior.nompumelelonzama.controller;

import com.enviro.assessment.junior.nompumelelonzama.entity.WithdrawalNotice;
import com.enviro.assessment.junior.nompumelelonzama.entity.WithdrawalStatus;
import com.enviro.assessment.junior.nompumelelonzama.service.WithdrawalHistoryService;
import com.enviro.assessment.junior.nompumelelonzama.service.WithdrawalService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final WithdrawalHistoryService withdrawalHistoryService;

    public WithdrawalController(WithdrawalService withdrawalService,
                                WithdrawalHistoryService withdrawalHistoryService) {
        this.withdrawalService = withdrawalService;
        this.withdrawalHistoryService = withdrawalHistoryService;
    }

    @PostMapping
    public WithdrawalNotice createWithdrawal(
            @RequestParam @NotNull(message = "portfolioId is required") @Positive(message = "portfolioId must be a positive number") Long portfolioId,
            @RequestParam @NotNull(message = "productId is required") @Positive(message = "productId must be a positive number") Long productId,
            @RequestParam @NotNull(message = "amount is required") @Positive(message = "amount must be greater than zero") BigDecimal amount) {
        return withdrawalService.createWithdrawal(portfolioId, productId, amount);
    }

    @GetMapping("/history/{portfolioId}")
    public List<WithdrawalNotice> getHistory(
            @PathVariable @Positive(message = "portfolioId must be a positive number") Long portfolioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) WithdrawalStatus status) {
        return withdrawalHistoryService.getHistory(portfolioId, start, end, status);
    }

    @GetMapping("/export/{portfolioId}")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable @Positive(message = "portfolioId must be a positive number") Long portfolioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) WithdrawalStatus status) {

        byte[] csvData = withdrawalHistoryService.generateCsv(portfolioId, start, end, status);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=withdrawal_statement.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}