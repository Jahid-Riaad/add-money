package org.ft.wallet.repository;

import org.ft.wallet.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository
        extends JpaRepository<Idempotency, String> {
}
