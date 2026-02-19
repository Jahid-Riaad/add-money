package org.ft.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferEvent {
    private UUID eventId;
    private String transactionId;
    private String customerId;
    private String bankAccountId;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}