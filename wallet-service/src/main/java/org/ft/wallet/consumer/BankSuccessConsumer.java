package org.ft.wallet.consumer;

import lombok.RequiredArgsConstructor;
import org.ft.wallet.dto.BankSuccessEvent;
import org.ft.wallet.entity.Wallet;
import org.ft.wallet.entity.InboxEvent;
import org.ft.wallet.repository.IdempotencyRepository;
import org.ft.wallet.repository.InboxRepository;
import org.ft.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BankSuccessConsumer {

    private final WalletRepository walletRepo;
    private final InboxRepository inboxRepo;
    private final IdempotencyRepository idempotencyRepo;
    private final ObjectMapper mapper;

    @Value("${wallet.default-currency}")
    private String defaultCurrency;

    @KafkaListener(topics = "${kafka.topic.bank-debit-success}")
    @Transactional
    public void consume(String message) throws Exception {

        BankSuccessEvent event =
                mapper.readValue(message, BankSuccessEvent.class);

        // Replay protection
        if(inboxRepo.existsById(event.getEventId())) return;

        Wallet wallet = walletRepo.findById(event.getCustomerId())
                .orElse(new Wallet(
                        event.getCustomerId(),
                        BigDecimal.ZERO,
                        defaultCurrency
                ));

        wallet.setBalance(wallet.getBalance().add(event.getAmount()));
        walletRepo.save(wallet);

        // Mark wallet idempotency SUCCESS
        idempotencyRepo.findById(event.getIdempotencyKey())
                .ifPresent(record -> {
                    record.setStatus("SUCCESS");
                    idempotencyRepo.save(record);
                });

        inboxRepo.save(
                new InboxEvent(
                        event.getEventId(),
                        LocalDateTime.now()
                )
        );
    }
}

