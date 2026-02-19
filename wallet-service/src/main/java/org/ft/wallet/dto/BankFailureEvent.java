package org.ft.wallet.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankFailureEvent {
    private String transactionId;
    private String customerId;
    private String reason;
}