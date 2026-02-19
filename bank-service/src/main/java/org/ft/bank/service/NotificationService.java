package org.ft.bank.service;

import org.ft.bank.dto.BankSuccessEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {

    @KafkaListener(topics = "bank.debit.success")
    public void sendNotification(BankSuccessEvent event) {
        System.out.println("**************************************************");
        System.out.println("NOTIFY: Dear " + event.getCustomerId() + ",");
        System.out.println("Your wallet top-up for TxID " + event.getTransactionId() + " was successful!");
        System.out.println("**************************************************");
    }
}