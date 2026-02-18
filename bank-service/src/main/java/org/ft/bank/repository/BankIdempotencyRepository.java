package org.ft.bank.repository;

import org.ft.bank.entity.BankIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankIdempotencyRepository
        extends JpaRepository<BankIdempotency, String> {
}
