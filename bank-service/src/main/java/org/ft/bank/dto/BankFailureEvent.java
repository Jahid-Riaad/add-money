package org.ft.bank.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankFailureEvent {
    private String idempotencyKey;
    private UUID transactionId;
    private String customerId;
    private String reason;
}