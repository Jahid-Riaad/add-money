package org.ft.bank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ft.bank.dto.TransferEvent;
import org.ft.bank.dto.WalletCreditSuccessEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    @KafkaListener(topics = "${kafka.topic.wallet-credit-success}", groupId = "notification-group-v3")
    public void sendNotification(ConsumerRecord<String, Object> record) {
        Object payload = record.value();
        log.info("RAW PAYLOAD RECEIVED: {}", payload);

        if (payload instanceof WalletCreditSuccessEvent event) {
            System.out.println(">>> NOTIFICATION: Dear " + event.getCustomerId() +
                    ", your top-up of " + event.getAmount() + " was successful!");
        }
        else if (payload instanceof TransferEvent transfer) {
            System.out.println(">>> NOTIFICATION (TransferEvent): Top-up successful for " + transfer.getCustomerId());
        }
        else {
            log.warn("Payload is neither SuccessEvent nor TransferEvent. Type: {}", payload.getClass().getName());
        }
    }
}