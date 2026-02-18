package org.ft.bank.consumer;

import lombok.RequiredArgsConstructor;
import org.ft.bank.dto.TransferEvent;
import org.ft.bank.service.BankService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferConsumer {
    private final BankService bankService;

    @KafkaListener(topics = "${kafka.topic.transfer-initiated}")
    @Transactional
    public void processTransfer(TransferEvent event){
        bankService.debit(
                event.getCustomerId(),
                event.getAmount(),
                event.getCurrency(),
                event.getIdempotencyKey()
        );
    }
}
