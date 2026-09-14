package com.enviro.assessment.junior.nompumelelonzama.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private BigDecimal balanceAfterWithdrawal;

    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    private Portfolio portfolio;
}