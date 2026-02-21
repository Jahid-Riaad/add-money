package org.ft.wallet.repository;

import org.ft.wallet.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<Idempotency, String> {
    Optional<Idempotency> findByIdempotencyKey(String idempotencyKey);
}
