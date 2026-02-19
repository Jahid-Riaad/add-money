package org.ft.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private String customerId;
    private Long bankAccountId;
    private BigDecimal amount;
    private String currency;
}