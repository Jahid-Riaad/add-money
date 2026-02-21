package org.ft.wallet.consumer;


import lombok.extern.slf4j.Slf4j;
import org.ft.wallet.dto.BankFailureEvent;
import org.ft.wallet.dto.BankSuccessEvent;
import org.ft.wallet.dto.WalletCreditSuccessEvent;
import org.ft.wallet.entity.Wallet;
import org.ft.wallet.entity.InboxEvent;
import org.ft.wallet.enumeration.IdempotencyStatus;
import org.ft.wallet.repository.IdempotencyRepository;
import org.ft.wallet.repository.InboxRepository;
import org.ft.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String,Object> kafkaTemplate;
    @Value("${wallet.default-currency}")
    private String defaultCurrency;

    @Value("${kafka.topic.wallet-credit-success}")
    private String creditSuccessTopic;

    @KafkaListener(topics = "${kafka.topic.bank-debit-success}")
    @Transactional
    public void handleBankSuccess(String message) throws Exception {
        BankSuccessEvent event = mapper.readValue(message, BankSuccessEvent.class);

        //Prevent double-crediting the wallet for the event
        if (inboxRepo.existsById(event.getEventId())) {
            log.info("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        //Update Wallet Balance
        Wallet wallet = walletRepo.findByCustomerId(event.getCustomerId())
                .orElseGet(
                        () -> {
                            log.info("Creating new wallet for customer: {}", event.getCustomerId());
                            return  walletRepo.save(new Wallet(event.getCustomerId(), BigDecimal.ZERO, "BDT"));
                        }
                );

        wallet.setBalance(wallet.getBalance().add(event.getAmount()));
        walletRepo.save(wallet);

        //Finalize Transaction Status in Wallet DB
        updateIdempotencyStatus(event.getIdempotencyKey(), IdempotencyStatus.SUCCESS);

        //Produce FINAL Success Event (The Receipt)
        WalletCreditSuccessEvent finalEvent = WalletCreditSuccessEvent.builder()
                .transactionId(event.getTransactionId())
                .customerId(event.getCustomerId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .status("COMPLETED")
                .completedAt(LocalDateTime.now().toString())
                .build();

        kafkaTemplate.send(creditSuccessTopic, event.getTransactionId().toString(), finalEvent);

        //Mark message as Seen in Inbox
        inboxRepo.save(new InboxEvent(event.getEventId(), LocalDateTime.now()));
    }

    @KafkaListener(topics = "${kafka.topic.bank-debit-failed}")
    @Transactional
    public void handleBankFailure(String message) throws Exception {
        BankFailureEvent event = mapper.readValue(message, BankFailureEvent.class);

        if (event.getTransactionId() == null) return;
        log.warn("Bank debit FAILED for Tx: {}. Reason: {}", event.getTransactionId(), event.getReason());
        updateIdempotencyStatus(event.getTransactionId(), IdempotencyStatus.FAILED);
    }

    private void updateIdempotencyStatus(String key, IdempotencyStatus status) {
        idempotencyRepo.findByIdempotencyKey(key).ifPresent(record -> {
            record.setStatus(status);
            idempotencyRepo.save(record);
            log.info("Updated Idempotency Key {} to status {}", key, status);
        });
    }
}

