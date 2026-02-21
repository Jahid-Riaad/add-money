package org.ft.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreditSuccessEvent {
    private UUID transactionId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime completedAt;
}