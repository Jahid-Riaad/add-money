package org.ft.wallet.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ft.wallet.enumeration.IdempotencyStatus;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse {
    private UUID transactionId;
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;
}