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
import org.springframework.transaction.annotation.Transactional;
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
            boolean success = bankService.debit(
                    event.getCustomerId(),
                    event.getAmount(),
                    event.getCurrency(),
                    event.getIdempotencyKey()
            );
            if (!success) {
                log.warn("Insufficient funds for Tx: {}. Sending failure event.", event.getIdempotencyKey());
                BankFailureEvent failure = BankFailureEvent.builder()
                        .transactionId(event.getIdempotencyKey())
                        .customerId(event.getCustomerId())
                        .reason("INSUFFICIENT_FUNDS")
                        .build();

                kafkaTemplate.send(failureTopic, failure);
            }
        } catch (RuntimeException e) {
                log.error("System error during processing Tx: {}. Triggering retry.", event.getIdempotencyKey());
                throw e;
            }
        }
    }
