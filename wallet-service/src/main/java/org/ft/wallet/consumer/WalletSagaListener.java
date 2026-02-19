package org.ft.wallet.consumer;


import lombok.extern.slf4j.Slf4j;
import org.ft.wallet.dto.BankFailureEvent;
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
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSagaListener {

    private final WalletRepository walletRepo;
    private final InboxRepository inboxRepo;
    private final IdempotencyRepository idempotencyRepo;
    private final ObjectMapper mapper;

    @Value("${wallet.default-currency}")
    private String defaultCurrency;


    @KafkaListener(topics = "${kafka.topic.bank-debit-success}")
    @Transactional
    public void handleBankSuccess(String message) throws Exception {
        BankSuccessEvent event = mapper.readValue(message, BankSuccessEvent.class);

        //Replay protection
        if (inboxRepo.existsById(event.getEventId())) {
            return;
        }

        //Update Wallet Balance
        Wallet wallet = walletRepo.findById(event.getCustomerId())
                .orElse(new Wallet(event.getCustomerId(), BigDecimal.ZERO, defaultCurrency));

        wallet.setBalance(wallet.getBalance().add(event.getAmount()));
        walletRepo.save(wallet);

        //Finalize Transaction Status
        updateIdempotencyStatus(event.getIdempotencyKey(), "SUCCESS");

        //Mark as processed in Inbox
        inboxRepo.save(new InboxEvent(event.getEventId(), LocalDateTime.now()));

        log.info("Wallet top-up COMPLETED for customer: {}", event.getCustomerId());
    }

    @KafkaListener(topics = "${kafka.topic.bank-debit-failed}")
    @Transactional
    public void handleBankFailure(String message) throws Exception {
        BankFailureEvent event = mapper.readValue(message, BankFailureEvent.class);

        if (event.getTransactionId() == null) return;
        log.warn("Bank debit FAILED for Tx: {}. Reason: {}", event.getTransactionId(), event.getReason());
        updateIdempotencyStatus(event.getTransactionId(), "FAILED");
    }

    private void updateIdempotencyStatus(String key, String status) {
        idempotencyRepo.findById(key).ifPresent(record -> {
            record.setStatus(status);
            idempotencyRepo.save(record);
        });
    }
}

