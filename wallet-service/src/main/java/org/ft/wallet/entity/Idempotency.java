package org.ft.wallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Idempotency {

    @Id
    private String idempotencyKey;

    private String status;

    private UUID transactionId;

    private LocalDateTime createdAt;
}
