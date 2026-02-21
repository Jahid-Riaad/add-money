package org.ft.wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ft.wallet.dto.*;
import org.ft.wallet.entity.Idempotency;
import org.ft.wallet.enumeration.IdempotencyStatus;
import org.ft.wallet.repository.IdempotencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdempotencyRepository idempotencyRepo;

    @Value("${idempotency.key-prefix}")
    private String idempotencyKeyPrefix;

    @Value("${kafka.topic.transfer-initiated}")
    private String transferTopic;

    @Transactional
    public TransferResponse initiateTopup(TransferRequest request, String idempotencyKey) {
        //Sanitize the Key
        final String sanitizedKey = idempotencyKey.startsWith(idempotencyKeyPrefix)
                ? idempotencyKey
                : idempotencyKeyPrefix + "-" + idempotencyKey;

        //Check for existing record using our NEW repository method
        Optional<Idempotency> existing = idempotencyRepo.findByIdempotencyKey(sanitizedKey);

        if (existing.isPresent()) {
            return handleDuplicateRequest(request, existing.get());
        }

        //New Transaction Flow
        UUID transactionId = UUID.randomUUID();

        //Save state as PROCESSING
        Idempotency newRecord = Idempotency.builder()
                .idempotencyKey(sanitizedKey)
                .status(IdempotencyStatus.PROCESSING)
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();

        idempotencyRepo.save(newRecord);

        // 5. Publish event
        publishTransferEvent(request, sanitizedKey, transactionId);

        return TransferResponse.builder()
                .transactionId(transactionId)
                .status(IdempotencyStatus.PROCESSING)
                .build();
    }

    private TransferResponse handleDuplicateRequest(TransferRequest request, Idempotency existingRecord) {
        String key = existingRecord.getIdempotencyKey();
        IdempotencyStatus existingStatus = existingRecord.getStatus();

        switch (existingStatus) {
            case SUCCESS ->
                    log.info("Request {} already succeeded. Returning cached result.", key);

            case PROCESSING -> {
                log.warn("Request {} is stuck in PROCESSING. Re-publishing event for Tx: {}", key, existingRecord.getTransactionId());
                publishTransferEvent(request, key, existingRecord.getTransactionId());
            }

            case FAILED ->
                    log.error("Request {} previously failed.", key);
        }

        return TransferResponse.builder()
                .transactionId(existingRecord.getTransactionId())
                .status(existingStatus)
                .build();
    }

    private void publishTransferEvent(TransferRequest request, String idempotencyKey, UUID txnId) {
        TransferEvent event = TransferEvent.builder()
                .source("BKASH")
                .eventId(UUID.randomUUID())
                .transactionId(txnId)
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .idempotencyKey(idempotencyKey)
                .build();

        kafkaTemplate.send(transferTopic, idempotencyKey, event);
    }
}

