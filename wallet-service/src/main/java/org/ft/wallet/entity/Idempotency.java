package org.ft.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ft.wallet.enumeration.IdempotencyStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ft_idempotency_keys", schema = "wallet")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Idempotency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    private UUID transactionId;
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;
    private LocalDateTime createdAt;
}
