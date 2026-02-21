package org.ft.bank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ft.bank.enumeration.IdempotencyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="ft_transaction_logs", schema = "bank")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idempotencyKey;
    private UUID transactionId;
    private String source;
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
