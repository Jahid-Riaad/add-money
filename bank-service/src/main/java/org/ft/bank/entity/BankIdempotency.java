package org.ft.bank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_idempotency")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankIdempotency {

    @Id
    private String idempotencyKey;

    private String status;

    private UUID transactionId;

    private LocalDateTime createdAt;
}
