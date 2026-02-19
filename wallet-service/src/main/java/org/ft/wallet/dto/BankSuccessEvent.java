package org.ft.wallet.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BankSuccessEvent {
    private UUID eventId;
    private String idempotencyKey;
    private String transactionId;
    private Long customerId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime successAt;
}