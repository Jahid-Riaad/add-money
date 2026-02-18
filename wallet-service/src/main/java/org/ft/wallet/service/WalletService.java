package org.ft.wallet.service;

import lombok.RequiredArgsConstructor;
import org.ft.wallet.dto.TransferEvent;
import org.ft.wallet.dto.TransferRequest;
import org.ft.wallet.dto.TransferResponse;
import org.ft.wallet.entity.Idempotency;
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
public class WalletService {

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final IdempotencyRepository idempotencyRepo;
    @Value("${fintech.idempotency.key-prefix}")
    private String keyPrefix;

    @Value("${kafka.topic.transfer-initiated}")
    private String transferTopic;

    @Transactional
    public TransferResponse initiateTopup(
            TransferRequest request,
            String idempotencyKey
    ){

        if(!idempotencyKey.startsWith(keyPrefix)) {
            idempotencyKey = keyPrefix + "-" + idempotencyKey;
        }

        //Check if already processed
        Optional<Idempotency> existing =
                idempotencyRepo.findById(idempotencyKey);

        if(existing.isPresent()) {
            return TransferResponse.builder()
                    .transactionId(existing.get().getTransactionId())
                    .status(existing.get().getStatus())
                    .build();
        }

        //Create transaction event
        UUID transactionId = UUID.randomUUID();

        TransferEvent event = TransferEvent.builder()
                .eventId(transactionId)
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .idempotencyKey(idempotencyKey)
                .build();

        //Save idempotency record BEFORE publish
        idempotencyRepo.save(
                new Idempotency(
                        idempotencyKey,
                        "PROCESSING",
                        transactionId,
                        LocalDateTime.now()
                )
        );

       kafkaTemplate.send(transferTopic,idempotencyKey,event);

        return TransferResponse.builder()
                .transactionId(transactionId)
                .status("PROCESSING")
                .build();
    }
}

