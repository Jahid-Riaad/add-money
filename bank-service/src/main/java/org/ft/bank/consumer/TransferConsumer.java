package org.ft.bank.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ft.bank.dto.BankFailureEvent;
import org.ft.bank.dto.TransferEvent;
import org.ft.bank.service.BankService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferConsumer {
    private final BankService bankService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${kafka.topic.bank-debit-failed}")
    private String failureTopic;

    @KafkaListener(topics = "${kafka.topic.transfer-initiated}")
    public void processTransfer(TransferEvent event) {
        try {
            boolean success = bankService.debit(event);

            if (!success) {
                log.warn("Debit FAILED (Insufficient Funds) for Tx: {}", event.getTransactionId());
                BankFailureEvent failure = BankFailureEvent.builder()
                        .transactionId(event.getTransactionId())
                        .idempotencyKey(event.getIdempotencyKey())
                        .customerId(event.getCustomerId())
                        .reason("INSUFFICIENT_FUNDS")
                        .build();

                kafkaTemplate.send(failureTopic, event.getTransactionId().toString(), failure);
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("currently being processed")) {
                log.info("Ignored duplicate retry for Tx: {} - already in progress.", event.getTransactionId());
                return;
            }
            log.error("Fatal system error for Tx: {}. Kafka will retry.", event.getTransactionId());
            throw e;
        }
    }
}
