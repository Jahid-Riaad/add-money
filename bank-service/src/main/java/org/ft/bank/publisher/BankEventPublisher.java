package org.ft.bank.publisher;

import lombok.RequiredArgsConstructor;
import org.ft.bank.dto.BankSuccessEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.bank-debit-success}")
    private String bankDebitSuccessTopic;

    public void publishBankSuccess(BankSuccessEvent event) {
        kafkaTemplate.send(
                bankDebitSuccessTopic,
                event.getTransactionId().toString(),
                event
        );
    }
}

