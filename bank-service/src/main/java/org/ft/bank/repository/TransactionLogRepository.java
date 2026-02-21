package org.ft.bank.repository;

import org.ft.bank.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TransactionLogRepository
        extends JpaRepository<TransactionLog, String> {
    Optional<TransactionLog> findBySourceAndIdempotencyKeyAndTransactionId(
            String source,
            String idempotencyKey,
            UUID transactionId
    );
}
